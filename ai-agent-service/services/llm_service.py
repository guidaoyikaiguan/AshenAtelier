from langchain_openai import ChatOpenAI
from tenacity import retry, stop_after_attempt, wait_exponential

from config import settings


_llm: ChatOpenAI | None = None


def get_llm() -> ChatOpenAI:
    """返回配置好的 LangChain ChatOpenAI 实例（支持 DeepSeek 等 OpenAI 兼容 API）。"""
    global _llm
    if _llm is not None:
        return _llm
    _llm = ChatOpenAI(
        model=settings.llm_model,
        api_key=settings.llm_api_key,
        base_url=settings.llm_base_url,
        temperature=0.3,
        max_tokens=2048,
    )
    return _llm


@retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
async def call_llm(messages: list[dict]) -> str:
    """带重试的 LLM 调用，返回文本响应。"""
    llm = get_llm()
    response = await llm.ainvoke([{"role": m["role"], "content": m["content"]} for m in messages])
    return response.content
