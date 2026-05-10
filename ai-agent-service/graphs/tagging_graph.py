import json
import re
from typing import TypedDict

from langgraph.graph import END, StateGraph

from prompts.tagging_prompts import TAGGING_SYSTEM_PROMPT, TAGGING_USER_TEMPLATE
from services.llm_service import call_llm


class TaggingState(TypedDict):
    title: str
    description: str
    existing_tags: str            # comma-separated
    existing_tag_set: set[str]
    all_db_tags: list[str]

    # LLM output
    llm_result_raw: str
    candidates: list[dict]       # [{"tag": ..., "relevance": ...}]

    # final
    suggestions: list[dict]      # sorted, filtered
    merged_tags: str             # final comma-separated


# --- Node: Generate Candidates ---

async def generate_candidates(state: TaggingState) -> TaggingState:
    """调用 LLM 生成候选标签。"""
    user_prompt = TAGGING_USER_TEMPLATE.format(
        title=state["title"],
        description=state.get("description", ""),
    )
    messages = [
        {"role": "system", "content": TAGGING_SYSTEM_PROMPT},
        {"role": "user", "content": user_prompt},
    ]
    raw = await call_llm(messages)
    state["llm_result_raw"] = raw

    try:
        cleaned = raw.strip()
        if cleaned.startswith("```"):
            cleaned = re.sub(r"^```\w*\n?", "", cleaned)
            cleaned = re.sub(r"\n```$", "", cleaned)
        parsed = json.loads(cleaned)
        state["candidates"] = parsed.get("tags", [])
    except (json.JSONDecodeError, AttributeError):
        state["candidates"] = []

    return state


# --- Node: Match Existing ---

def match_existing(state: TaggingState) -> TaggingState:
    """标准化候选标签并与现有标签去重。"""
    existing = state.get("existing_tag_set", set())
    all_db = state.get("all_db_tags", [])
    cleaned: list[dict] = []

    for item in state["candidates"]:
        tag = item.get("tag", "").strip()
        relevance = float(item.get("relevance", 0.5))

        # 跳过空标签和已存在的标签
        if not tag or tag in existing:
            continue

        # 模糊匹配 DB 中的标签（包含关系）
        matched = False
        for db_tag in all_db:
            if tag in db_tag or db_tag in tag:
                tag = db_tag  # 用已有标签名
                matched = True
                break

        if matched:
            # 检查去重
            if tag not in [c["tag"] for c in cleaned]:
                cleaned.append({"tag": tag, "relevance": relevance})
        else:
            cleaned.append({"tag": tag, "relevance": relevance})

    state["candidates"] = cleaned
    return state


# --- Node: Rank & Select ---

def rank_and_select(state: TaggingState) -> TaggingState:
    """按相关度排序，过滤低分，取 Top-N。"""
    candidates = state["candidates"]

    # 过滤 relevance < 0.3
    filtered = [c for c in candidates if c["relevance"] >= 0.3]

    # 按相关度降序排列
    filtered.sort(key=lambda x: x["relevance"], reverse=True)

    # 取 Top 5-8
    selected = filtered[:8]
    if len(selected) < 5:
        selected = filtered[:5]

    state["suggestions"] = selected

    # 合并标签
    existing = state.get("existing_tag_set", set())
    all_tags = list(existing) + [s["tag"] for s in selected]
    # 去重保序
    seen = set()
    unique = []
    for t in all_tags:
        if t not in seen:
            seen.add(t)
            unique.append(t)
    state["merged_tags"] = ",".join(unique)

    return state


# --- Build graph ---

def build_tagging_graph() -> StateGraph:
    graph = StateGraph(TaggingState)

    graph.add_node("generate_candidates", generate_candidates)
    graph.add_node("match_existing", match_existing)
    graph.add_node("rank_and_select", rank_and_select)

    graph.set_entry_point("generate_candidates")
    graph.add_edge("generate_candidates", "match_existing")
    graph.add_edge("match_existing", "rank_and_select")
    graph.add_edge("rank_and_select", END)

    return graph.compile()
