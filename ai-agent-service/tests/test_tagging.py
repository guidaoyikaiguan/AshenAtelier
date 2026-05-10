import pytest
from unittest.mock import AsyncMock, patch

from models.schemas import TagEnrichRequest, TagEnrichResponse, TagSuggestResponse, TagSuggestion


class TestTaggingGraph:
    def test_rank_filter_low(self):
        from graphs.tagging_graph import rank_and_select
        state = {
            "title": "", "description": "", "existing_tags": "", "existing_tag_set": set(),
            "all_db_tags": [], "llm_result_raw": "",
            "candidates": [
                {"tag": "高分标签", "relevance": 0.95},
                {"tag": "低分标签", "relevance": 0.2},
                {"tag": "一般标签", "relevance": 0.6},
            ],
            "suggestions": [], "merged_tags": "",
        }
        result = rank_and_select(state)
        tags = [s["tag"] for s in result["suggestions"]]
        assert "高分标签" in tags
        assert "低分标签" not in tags

    def test_match_existing_dedup(self):
        from graphs.tagging_graph import match_existing
        state = {
            "title": "", "description": "", "existing_tags": "教程", "existing_tag_set": {"教程"},
            "all_db_tags": ["编程", "Python"],
            "llm_result_raw": "",
            "candidates": [
                {"tag": "教程", "relevance": 0.95},
                {"tag": "编程", "relevance": 0.9},
                {"tag": "新标签", "relevance": 0.8},
            ],
            "suggestions": [], "merged_tags": "",
        }
        result = match_existing(state)
        tags = [c["tag"] for c in result["candidates"]]
        assert "教程" not in tags
        assert "编程" in tags
        assert "新标签" in tags


class TestTaggingAgent:
    @pytest.mark.asyncio
    async def test_suggest(self):
        from agents.tagging_agent import tagging_agent
        with patch("graphs.tagging_graph.call_llm", new_callable=AsyncMock) as mock_llm:
            mock_llm.return_value = '{"tags": [{"tag": "编程", "relevance": 0.95}, {"tag": "Python", "relevance": 0.9}]}'
            with patch.object(tagging_agent, "_load_all_tags", new_callable=AsyncMock) as mock_load:
                mock_load.return_value = []
                result = await tagging_agent.suggest("Python入门教程", "适合初学者的Python编程教程")
                assert len(result.suggestions) > 0

    @pytest.mark.asyncio
    async def test_enrich(self):
        from agents.tagging_agent import tagging_agent
        with patch("graphs.tagging_graph.call_llm", new_callable=AsyncMock) as mock_llm:
            mock_llm.return_value = '{"tags": [{"tag": "游戏", "relevance": 0.95}]}'
            with patch.object(tagging_agent, "_load_all_tags", new_callable=AsyncMock) as mock_load:
                mock_load.return_value = []
                req = TagEnrichRequest(video_id=1, title="Minecraft实况", description="MC实况")
                result = await tagging_agent.enrich(req)
                assert len(result.suggested_tags) > 0


class TestTaggingAPI:
    def test_suggest_endpoint(self, client):
        from agents import tagging_agent as ta
        mock_resp = TagSuggestResponse(
            suggestions=[TagSuggestion(tag="教程", relevance=0.95)]
        )
        async def fake_suggest(self, title, description=""):
            return mock_resp

        with patch.object(ta.tagging_agent.__class__, "suggest", fake_suggest):
            resp = client.get("/api/ai/tagging/suggest?title=Python教程")
            assert resp.status_code == 200
            assert resp.json()["status"] == "success"

    def test_enrich_endpoint(self, client):
        from agents import tagging_agent as ta
        mock_resp = TagEnrichResponse(video_id=1, suggested_tags=["教程"], merged_tags="教程")
        async def fake_enrich(self, req):
            return mock_resp

        with patch.object(ta.tagging_agent.__class__, "enrich", fake_enrich):
            resp = client.post("/api/ai/tagging/enrich", json={
                "video_id": 1, "title": "测试", "description": "测试", "existing_tags": ""
            })
            assert resp.status_code == 200
            assert resp.json()["status"] == "success"
