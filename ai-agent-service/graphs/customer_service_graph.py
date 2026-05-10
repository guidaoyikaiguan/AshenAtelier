import json
import re
from typing import TypedDict

from langgraph.graph import END, StateGraph

from prompts.customer_service_prompts import CHAT_USER_TEMPLATE, CUSTOMER_SERVICE_SYSTEM_PROMPT
from services.llm_service import call_llm


class CustomerServiceState(TypedDict):
    user_id: int
    message: str

    llm_result_raw: str

    intent: str
    reply: str
    confidence: float
    should_escalate: bool


# --- Node: Parse Intent + Generate Reply ---

async def parse_and_reply(state: CustomerServiceState) -> CustomerServiceState:
    """一次 LLM 调用完成意图解析和回复生成。"""
    messages = [
        {"role": "system", "content": CUSTOMER_SERVICE_SYSTEM_PROMPT},
        {"role": "user", "content": CHAT_USER_TEMPLATE.format(message=state["message"])},
    ]
    raw = await call_llm(messages)
    state["llm_result_raw"] = raw

    try:
        cleaned = raw.strip()
        if cleaned.startswith("```"):
            cleaned = re.sub(r"^```\w*\n?", "", cleaned)
            cleaned = re.sub(r"\n```$", "", cleaned)
        parsed = json.loads(cleaned)
        state["intent"] = parsed.get("intent", "general")
        state["reply"] = parsed.get("reply", "抱歉，我暂时无法回答这个问题，请稍后再试。")
        state["confidence"] = float(parsed.get("confidence", 0.5))
        state["should_escalate"] = bool(parsed.get("should_escalate", False))
    except (json.JSONDecodeError, AttributeError):
        state["intent"] = "general"
        state["reply"] = "抱歉，我暂时无法回答这个问题，请尝试联系管理员获取帮助。"
        state["confidence"] = 0.3
        state["should_escalate"] = True

    return state


# --- Node: Check Escalation ---

def check_escalation(state: CustomerServiceState) -> CustomerServiceState:
    """低置信度时建议转人工。"""
    if state["confidence"] < 0.5:
        state["should_escalate"] = True
        if not state["reply"]:
            state["reply"] = "抱歉，我对此问题不太确定，建议联系人工客服获取更准确的帮助。"

    if state["should_escalate"]:
        prefix = "（提示：如果以上回答未能解决您的问题，建议联系人工客服）\n\n"
        state["reply"] = prefix + state["reply"]

    return state


# --- Build graph ---

def build_customer_service_graph() -> StateGraph:
    graph = StateGraph(CustomerServiceState)

    graph.add_node("parse_and_reply", parse_and_reply)
    graph.add_node("check_escalation", check_escalation)

    graph.set_entry_point("parse_and_reply")
    graph.add_edge("parse_and_reply", "check_escalation")
    graph.add_edge("check_escalation", END)

    return graph.compile()
