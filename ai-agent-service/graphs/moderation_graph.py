import json
import re
from typing import TypedDict

from langgraph.graph import END, StateGraph

from models.enums import ContentType, ModerationResult
from prompts.moderation_prompts import (
    COMMENT_MODERATION_TEMPLATE,
    DANMAKU_MODERATION_TEMPLATE,
    MODERATION_SYSTEM_PROMPT,
    SENSITIVE_WORDS,
    VIDEO_MODERATION_TEMPLATE,
)
from services.llm_service import call_llm


class ModerationState(TypedDict):
    content_type: str       # video / comment / danmaku
    content_id: int
    title: str
    description: str
    tags: str
    content: str            # comment or danmaku text

    # pre-screen
    pre_screen_matched: bool
    pre_screen_categories: list[str]

    # LLM review
    llm_result_raw: str

    # final
    result: str
    confidence: float
    risk_categories: list[str]
    review_note: str


# --- Node: Pre-screen ---

def pre_screen(state: ModerationState) -> ModerationState:
    """敏感词字典 + 正则预筛。"""
    text = _build_text(state)
    matched_categories: list[str] = []

    for category, words in SENSITIVE_WORDS.items():
        for word in words:
            if word.lower() in text.lower():
                if category not in matched_categories:
                    matched_categories.append(category)

    # 手机号正则
    phone_pattern = r"1[3-9]\d{9}"
    if re.search(phone_pattern, text):
        if "personal_info" not in matched_categories:
            matched_categories.append("personal_info")

    state["pre_screen_matched"] = len(matched_categories) > 0
    state["pre_screen_categories"] = matched_categories
    return state


# --- Node: LLM Deep Review ---

async def llm_deep_review(state: ModerationState) -> ModerationState:
    """调用 LLM 深度审核。"""
    if state["content_type"] == ContentType.VIDEO:
        user_prompt = VIDEO_MODERATION_TEMPLATE.format(
            title=state.get("title", ""),
            description=state.get("description", ""),
            tags=state.get("tags", ""),
        )
    elif state["content_type"] == ContentType.COMMENT:
        user_prompt = COMMENT_MODERATION_TEMPLATE.format(content=state.get("content", ""))
    else:
        user_prompt = DANMAKU_MODERATION_TEMPLATE.format(content=state.get("content", ""))

    messages = [
        {"role": "system", "content": MODERATION_SYSTEM_PROMPT},
        {"role": "user", "content": user_prompt},
    ]

    raw = await call_llm(messages)
    state["llm_result_raw"] = raw
    return state


# --- Node: Decide Action ---

def decide_action(state: ModerationState) -> ModerationState:
    """解析 LLM 结果，做最终决策。"""

    # 如果预筛匹配到，直接评估风险
    if state["pre_screen_matched"]:
        high_risk = any(
            cat in state["pre_screen_categories"]
            for cat in ["politics", "adult", "violence"]
        )
        if high_risk:
            state["result"] = ModerationResult.REJECTED
            state["confidence"] = 0.95
            state["risk_categories"] = state["pre_screen_categories"]
            state["review_note"] = f"敏感词预筛命中: {', '.join(state['pre_screen_categories'])}"
        else:
            state["result"] = ModerationResult.PENDING
            state["confidence"] = 0.6
            state["risk_categories"] = state["pre_screen_categories"]
            state["review_note"] = f"敏感词预筛命中(中风险): {', '.join(state['pre_screen_categories'])}"
        return state

    # 解析 LLM JSON 结果
    try:
        raw = state.get("llm_result_raw", "{}").strip()
        # 清理可能的 markdown 代码块包裹
        if raw.startswith("```"):
            raw = raw.split("\n", 1)[1]
            if raw.endswith("```"):
                raw = raw[:-3]
        parsed = json.loads(raw)
    except (json.JSONDecodeError, AttributeError):
        state["result"] = ModerationResult.PENDING
        state["confidence"] = 0.5
        state["risk_categories"] = []
        state["review_note"] = "LLM 输出格式解析失败，标记为待审核"
        return state

    is_safe = parsed.get("is_safe", True)
    confidence = float(parsed.get("confidence", 0.8))
    categories = parsed.get("risk_categories", [])
    note = parsed.get("review_note", "")

    if is_safe and confidence >= 0.9:
        state["result"] = ModerationResult.APPROVED
    elif not is_safe and confidence >= 0.7:
        state["result"] = ModerationResult.REJECTED
    else:
        state["result"] = ModerationResult.PENDING

    state["confidence"] = confidence
    state["risk_categories"] = categories
    state["review_note"] = note
    return state


def _build_text(state: ModerationState) -> str:
    parts = []
    if state.get("title"):
        parts.append(state["title"])
    if state.get("description"):
        parts.append(state["description"])
    if state.get("tags"):
        parts.append(state["tags"])
    if state.get("content"):
        parts.append(state["content"])
    return " ".join(parts)


# --- Build graph ---

def build_moderation_graph() -> StateGraph:
    graph = StateGraph(ModerationState)

    graph.add_node("pre_screen", pre_screen)
    graph.add_node("llm_deep_review", llm_deep_review)
    graph.add_node("decide_action", decide_action)

    graph.set_entry_point("pre_screen")

    # 预筛未命中 → LLM深度审核；预筛命中 → 直接决策
    graph.add_conditional_edges(
        "pre_screen",
        lambda s: "decide_action" if s["pre_screen_matched"] else "llm_deep_review",
        {"llm_deep_review": "llm_deep_review", "decide_action": "decide_action"},
    )

    graph.add_edge("llm_deep_review", "decide_action")
    graph.add_edge("decide_action", END)

    return graph.compile()
