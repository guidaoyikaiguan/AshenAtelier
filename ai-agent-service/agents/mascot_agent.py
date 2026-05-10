from graphs.mascot_graph import build_mascot_graph
from models.schemas import MascotChatRequest, MascotChatResponse


class MascotAgent:
    def __init__(self):
        self.graph = build_mascot_graph()

    async def chat(self, req: MascotChatRequest) -> MascotChatResponse:
        state = {
            "message": req.message,
            "conversation_history": req.conversation_history or [],
            "video_context": req.video_context,
            "transcript": req.transcript or "",
            "llm_result_raw": "",
            "mood": "default",
            "reply": "",
            "reply_ja": "",
        }
        result = await self.graph.ainvoke(state)
        return MascotChatResponse(
            mood=result["mood"],
            reply=result["reply"],
            reply_ja=result.get("reply_ja", ""),
        )


mascot_agent = MascotAgent()
