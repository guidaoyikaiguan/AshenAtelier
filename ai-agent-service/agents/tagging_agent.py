from graphs.tagging_graph import build_tagging_graph
from models.schemas import TagEnrichRequest, TagEnrichResponse, TagSuggestResponse, TagSuggestion


class TaggingAgent:
    def __init__(self):
        self.graph = build_tagging_graph()

    async def suggest(self, title: str, description: str = "") -> TagSuggestResponse:
        """实时标签建议（上传页用）。"""
        state = {
            "title": title,
            "description": description,
            "existing_tags": "",
            "existing_tag_set": set(),
            "all_db_tags": await self._load_all_tags(),
            "llm_result_raw": "",
            "candidates": [],
            "suggestions": [],
            "merged_tags": "",
        }

        result = await self.graph.ainvoke(state)

        suggestions = [
            TagSuggestion(tag=s["tag"], relevance=s["relevance"])
            for s in result["suggestions"]
        ]
        return TagSuggestResponse(suggestions=suggestions)

    async def enrich(self, req: TagEnrichRequest) -> TagEnrichResponse:
        """事后补充标签（Java 异步调用）。"""
        existing_tags = req.existing_tags or ""
        existing_set = set(existing_tags.split(",")) if existing_tags else set()

        state = {
            "title": req.title,
            "description": req.description or "",
            "existing_tags": existing_tags,
            "existing_tag_set": existing_set,
            "all_db_tags": await self._load_all_tags(),
            "llm_result_raw": "",
            "candidates": [],
            "suggestions": [],
            "merged_tags": "",
        }

        result = await self.graph.ainvoke(state)

        return TagEnrichResponse(
            video_id=req.video_id,
            suggested_tags=[s["tag"] for s in result["suggestions"]],
            merged_tags=result["merged_tags"],
        )

    async def _load_all_tags(self) -> list[str]:
        """从数据库加载所有已有标签，用于标准化匹配。"""
        try:
            from services.storage_service import storage_service
            tags = storage_service.get_all_tags()
            return tags
        except Exception:
            return []


# Singleton
tagging_agent = TaggingAgent()
