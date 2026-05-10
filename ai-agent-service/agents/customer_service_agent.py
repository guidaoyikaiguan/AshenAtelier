from graphs.customer_service_graph import build_customer_service_graph
from models.schemas import ChatRequest, ChatResponse


class CustomerServiceAgent:
    def __init__(self):
        self.graph = build_customer_service_graph()

    async def chat(self, req: ChatRequest) -> ChatResponse:
        state = {
            "user_id": req.user_id,
            "message": req.message,
            "llm_result_raw": "",
            "intent": "general",
            "reply": "",
            "confidence": 0.5,
            "should_escalate": False,
        }

        result = await self.graph.ainvoke(state)

        return ChatResponse(
            reply=result["reply"],
            intent=result["intent"],
            confidence=result["confidence"],
            should_escalate=result["should_escalate"],
        )


# Singleton
customer_service_agent = CustomerServiceAgent()
