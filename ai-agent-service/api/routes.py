import asyncio
import json
import logging
import traceback

import httpx
from fastapi import APIRouter, File, Query, UploadFile
from fastapi.responses import StreamingResponse

from agents.customer_service_agent import customer_service_agent
from agents.mascot_agent import mascot_agent
from agents.moderation_agent import moderation_agent
from agents.tagging_agent import tagging_agent
from integrations.shipin_client import shipin_client
from models.enums import ContentType, ModerationResult
from models.schemas import (
    ApiResponse,
    ChatRequest,
    MascotChatRequest,
    MascotHistoryRequest,
    ModerationLog,
    ModerationRequest,
    TagEnrichRequest,
)
from services.asr_service import asr_service
from services.mascot_history_service import mascot_history_service
from services.storage_service import storage_service

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/ai")


# ============================================================
# Health
# ============================================================

@router.get("/health")
async def health_check():
    return ApiResponse.ok(data={"status": "healthy", "service": "ai-agent-service"})


# ============================================================
# Moderation
# ============================================================

@router.post("/moderation/video")
async def moderate_video(req: ModerationRequest):
    req.content_type = ContentType.VIDEO
    result = await moderation_agent.moderate(req)
    _save_log(result)
    # 回调 Java 更新视频审核状态
    _callback_video_status(result)
    return ApiResponse.ok(data=result.model_dump())


@router.post("/moderation/comment")
async def moderate_comment(req: ModerationRequest):
    req.content_type = ContentType.COMMENT
    result = await moderation_agent.moderate(req)
    _save_log(result)
    return ApiResponse.ok(data=result.model_dump())


@router.post("/moderation/danmaku")
async def moderate_danmaku(req: ModerationRequest):
    req.content_type = ContentType.DANMAKU
    result = await moderation_agent.moderate(req)
    _save_log(result)
    return ApiResponse.ok(data=result.model_dump())


@router.get("/moderation/log/{content_type}/{content_id}")
async def get_moderation_log(content_type: str, content_id: int):
    log = storage_service.get_moderation_log(content_type, content_id)
    return ApiResponse.ok(data={"log": log})


def _save_log(response):
    try:
        storage_service.save_moderation_log(ModerationLog(
            content_type=response.content_type,
            content_id=response.content_id,
            result=response.result,
            confidence=response.confidence,
            risk_categories=",".join(response.risk_categories),
            review_note=response.review_note,
        ))
    except Exception:
        logger.exception("Failed to save moderation log")


def _callback_video_status(result):
    """回调 Java 后端更新视频审核状态（异步 fire-and-forget）。"""
    async def _do():
        try:
            status_map = {
                ModerationResult.APPROVED: "1",
                ModerationResult.REJECTED: "2",
                ModerationResult.PENDING: "0",
            }
            status = status_map.get(result.result, "0")
            logger.info("Calling updateVideoStatus: content_id=%s, status=%s, note=%s",
                        result.content_id, status, result.review_note or "")
            resp = await shipin_client.update_video_status(
                result.content_id, status, result.review_note or ""
            )
            logger.info("updateVideoStatus response: %s", resp)
        except Exception:
            logger.exception("AI callback updateVideoStatus FAILED for content_id=%s",
                             result.content_id)
    task = asyncio.ensure_future(_do())
    # 保存 task 引用防止被 GC 取消
    if not hasattr(_callback_video_status, "_tasks"):
        _callback_video_status._tasks = []
    _callback_video_status._tasks.append(task)


# ============================================================
# Tagging
# ============================================================

@router.get("/tagging/suggest")
async def suggest_tags(
    title: str = Query(..., description="视频标题"),
    description: str = Query("", description="视频描述"),
):
    result = await tagging_agent.suggest(title, description)
    return ApiResponse.ok(data=result.model_dump())


@router.post("/tagging/enrich")
async def enrich_tags(req: TagEnrichRequest):
    result = await tagging_agent.enrich(req)
    async def _do():
        try:
            merged = result.merged_tags
            if isinstance(merged, list):
                merged = ",".join(merged)
            logger.info("Calling updateVideoTags: video_id=%s, tags=%s", req.video_id, merged)
            resp = await shipin_client.update_video_tags(req.video_id, merged)
            logger.info("updateVideoTags response: %s", resp)
        except Exception:
            logger.exception("AI callback updateVideoTags FAILED for video_id=%s", req.video_id)
    task = asyncio.ensure_future(_do())
    if not hasattr(enrich_tags, "_tasks"):
        enrich_tags._tasks = []
    enrich_tags._tasks.append(task)
    return ApiResponse.ok(data=result.model_dump())


# ============================================================
# Customer Service
# ============================================================

@router.post("/chat/message")
async def chat_message(req: ChatRequest):
    result = await customer_service_agent.chat(req)
    return ApiResponse.ok(data=result.model_dump())


# ============================================================
# Mascot
# ============================================================

@router.post("/mascot/chat")
async def mascot_chat(req: MascotChatRequest):
    """SSE streaming mascot chat endpoint.

    Events:
      type: "mood"   | expression: "happy", reply_ja: "こんにちは"
      type: "token"  | content: "你好呀"
      type: "done"
      type: "error"  | message: "..."
    """
    async def event_stream():
        try:
            result = await mascot_agent.chat(req)
            yield f"data: {json.dumps({'type': 'mood', 'expression': result.mood, 'reply_ja': result.reply_ja or ''})}\n\n"
            for i in range(0, len(result.reply), 3):
                chunk = result.reply[i:i + 3]
                yield f"data: {json.dumps({'type': 'token', 'content': chunk})}\n\n"
                await asyncio.sleep(0.03)
            yield f"data: {json.dumps({'type': 'done'})}\n\n"
        except Exception as e:
            yield f"data: {json.dumps({'type': 'error', 'message': str(e)})}\n\n"

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


# Mascot history persistence
@router.get("/mascot/history")
async def get_mascot_history(mascot_id: str = Query(..., description="Anonymous mascot user ID")):
    try:
        history = await mascot_history_service.get_history(mascot_id)
        return ApiResponse.ok(data={"history": history})
    except Exception:
        return ApiResponse.ok(data={"history": []})


@router.post("/mascot/history")
async def save_mascot_history(req: MascotHistoryRequest):
    try:
        await mascot_history_service.save_history(req.mascot_id, req.history)
        return ApiResponse.ok(msg="saved")
    except Exception:
        return ApiResponse.fail(code=500, msg="Failed to save history")


# Video transcript via Faster-Whisper
@router.post("/video/transcript")
async def video_transcript(video_id: int = Query(..., description="Video ID")):
    """Transcribe video audio to text. Cached in memory."""
    from services.video_transcript_service import get_video_transcript
    text = await get_video_transcript(str(video_id))
    return ApiResponse.ok(data={"video_id": video_id, "transcript": text})


# ASR (Speech-to-Text) via Faster-Whisper
@router.post("/mascot/asr")
async def mascot_asr(audio: UploadFile = File(...)):
    """Receive audio from browser and return transcribed text."""
    if os.getenv("ASR_ENABLED", "true").lower() == "false":
        return ApiResponse.fail(code=503, msg="ASR service not available in this deployment")
    audio_bytes = await audio.read()
    text = await asr_service.transcribe(audio_bytes)
    return ApiResponse.ok(data={"text": text})


# GPT-SoVITS TTS proxy
import os
import random
import glob

TTS_API_URL = "http://127.0.0.1:9880/tts"
TTS_REF_DIR = "D:/tts_ref"
TTS_PROMPT_LANG = "ja"


def _pick_ref_audio(mood: str) -> tuple[str, str]:
    """Pick a random .wav from the mood folder. Returns (ref_audio_path, prompt_text).
    Falls back to 'default' if the mood folder is empty or missing.
    Filters out files outside GPT-SoVITS 3~10s duration range."""
    import subprocess
    for folder in (mood, "default"):
        pattern = os.path.join(TTS_REF_DIR, folder, "*.wav")
        files = [f for f in glob.glob(pattern) if f.endswith(".wav")]
        valid_files = []
        for f in files:
            try:
                result = subprocess.run(
                    ["ffprobe", "-v", "quiet", "-show_entries", "format=duration",
                     "-of", "csv=p=0", f],
                    capture_output=True, text=True, timeout=10
                )
                dur = float(result.stdout.strip())
                if 3.0 <= dur <= 10.0:
                    valid_files.append(f)
            except Exception:
                pass
        if valid_files:
            chosen = random.choice(valid_files).replace("\\", "/")
            prompt = os.path.splitext(os.path.basename(chosen))[0]
            return chosen, prompt
    raise FileNotFoundError(f"No .wav files found in {TTS_REF_DIR}/ (checked mood '{mood}' and 'default')")


@router.get("/mascot/tts")
async def mascot_tts(text: str = "", text_lang: str = "ja", mood: str = "default"):
    """Proxy to local GPT-SoVITS TTS. Picks a random reference audio matching the mood."""
    if os.getenv("TTS_ENABLED", "true").lower() == "false":
        return ApiResponse.fail(code=503, msg="TTS service not available in this deployment")
    ref_path, prompt_text = _pick_ref_audio(mood)
    async with httpx.AsyncClient(timeout=120) as client:
        resp = await client.get(TTS_API_URL, params={
            "text": text,
            "text_lang": text_lang,
            "ref_audio_path": ref_path,
            "prompt_lang": TTS_PROMPT_LANG,
            "prompt_text": prompt_text,
        })
        if resp.status_code != 200:
            return ApiResponse.fail(code=resp.status_code, msg=f"TTS engine returned {resp.status_code}")
        # Read all bytes first — GPT-SoVITS can be slow and streaming confuses Audio element
        audio_bytes = await resp.aread()
        return StreamingResponse(
            iter([audio_bytes]),
            media_type="audio/wav",
        )
