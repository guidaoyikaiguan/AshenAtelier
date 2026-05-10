import os
import subprocess
import tempfile

import httpx

from config import settings

_transcript_cache: dict[str, str] = {}


async def get_video_transcript(video_id: str) -> str:
    """Extract audio from video, transcribe with Faster-Whisper, cache result."""
    cache_key = str(video_id)
    if cache_key in _transcript_cache:
        return _transcript_cache[cache_key]

    # Get video URL from Java backend
    from integrations.shipin_client import shipin_client
    video = await shipin_client.get_video(int(video_id))
    video_url = video.get("data", {}).get("videoUrl", "")
    if not video_url:
        return ""
    if not video_url.startswith("http"):
        video_url = f"{settings.shipin_service_url}{video_url}"

    # Download video and extract audio in one shot with ffmpeg
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp_path = tmp.name

    try:
        result = subprocess.run(
            ["ffmpeg", "-y", "-i", video_url, "-ar", "16000", "-ac", "1",
             "-f", "wav", "-t", "600", tmp_path],
            capture_output=True, text=True, timeout=300
        )
        if not os.path.exists(tmp_path) or os.path.getsize(tmp_path) < 1000:
            return ""

        from services.asr_service import asr_service
        with open(tmp_path, "rb") as f:
            text = await asr_service.transcribe(f.read())
        _transcript_cache[cache_key] = text
        return text
    except Exception:
        return ""
    finally:
        try:
            os.unlink(tmp_path)
        except OSError:
            pass
