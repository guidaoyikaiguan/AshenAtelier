from graphs.moderation_graph import build_moderation_graph
from models.enums import ContentType
from models.schemas import ModerationRequest, ModerationResponse


class ModerationAgent:
    def __init__(self):
        self.graph = build_moderation_graph()

    async def moderate(self, req: ModerationRequest) -> ModerationResponse:
        state = {
            "content_type": req.content_type,
            "content_id": req.content_id,
            "title": req.title or "",
            "description": req.description or "",
            "tags": req.tags or "",
            "content": req.content or "",
            "pre_screen_matched": False,
            "pre_screen_categories": [],
            "llm_result_raw": "",
            "result": "",
            "confidence": 0.0,
            "risk_categories": [],
            "review_note": "",
        }

        result_state = await self.graph.ainvoke(state)

        return ModerationResponse(
            content_type=req.content_type,
            content_id=req.content_id,
            result=result_state["result"],
            confidence=result_state["confidence"],
            risk_categories=result_state["risk_categories"],
            review_note=result_state["review_note"],
        )


# Singleton
moderation_agent = ModerationAgent()
