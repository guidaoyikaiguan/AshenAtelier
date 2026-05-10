from typing import TypedDict

from langgraph.graph import END, StateGraph

from prompts.mascot_prompts import (
    MASCOT_SYSTEM_PROMPT,
    MASCOT_TRANSCRIPT_TMPL,
    MASCOT_USER_TEMPLATE,
    MASCOT_VIDEO_CONTEXT_TMPL,
)
from services.llm_service import call_llm


class MascotState(TypedDict):
    message: str
    conversation_history: list[dict]
    video_context: dict | None
    transcript: str
    llm_result_raw: str
    mood: str
    reply: str
    reply_ja: str


VALID_MOODS = {"default", "happy", "angry", "sad", "surprised", "shy", "thinking"}


def _format_history(history: list[dict], max_turns: int = 5) -> str:
    recent = history[-(max_turns * 2):]
    lines = []
    for msg in recent:
        role_label = "旅人" if msg["role"] == "user" else "伊蕾娜"
        lines.append(f"{role_label}：{msg['content']}")
    return "\n".join(lines) + "\n" if lines else ""


def _format_video_context(ctx: dict | None, transcript: str = "") -> str:
    if not ctx:
        return ""
    transcript_text = ""
    if transcript and transcript.strip():
        transcript_text = MASCOT_TRANSCRIPT_TMPL.format(text=transcript.strip())
    return MASCOT_VIDEO_CONTEXT_TMPL.format(
        title=ctx.get("title", ""),
        description=ctx.get("description", ""),
        tags=ctx.get("tags", ""),
        transcript=transcript_text,
    )


async def generate_reply(state: MascotState) -> MascotState:
    history_text = _format_history(state.get("conversation_history", []))
    video_text = _format_video_context(state.get("video_context"), state.get("transcript", ""))
    user_content = MASCOT_USER_TEMPLATE.format(
        history_text=history_text,
        video_context=video_text,
        message=state["message"],
    )
    messages = [
        {"role": "system", "content": MASCOT_SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]
    state["llm_result_raw"] = await call_llm(messages)
    return state


async def parse_mood(state: MascotState) -> MascotState:
    raw = state["llm_result_raw"].strip()
    parts = raw.split("|", 2)
    if len(parts) >= 2 and parts[0].strip() in VALID_MOODS:
        state["mood"] = parts[0].strip()
        state["reply"] = parts[1].strip()
        state["reply_ja"] = parts[2].strip() if len(parts) >= 3 else ""
    else:
        state["mood"] = "default"
        state["reply"] = raw.replace("default|", "").strip() or raw.strip()
        state["reply_ja"] = ""
    return state


def build_mascot_graph() -> StateGraph:
    graph = StateGraph(MascotState)
    graph.add_node("generate_reply", generate_reply)
    graph.add_node("parse_mood", parse_mood)
    graph.set_entry_point("generate_reply")
    graph.add_edge("generate_reply", "parse_mood")
    graph.add_edge("parse_mood", END)
    return graph.compile()
