import pytest
from unittest.mock import AsyncMock, patch

from models.schemas import ModerationRequest, ModerationResponse
from models.enums import ContentType


@pytest.fixture
def video_request():
    return ModerationRequest(
        content_type=ContentType.VIDEO,
        content_id=1,
        title="Python入门教程",
        description="学习Python基础编程",
        tags="教程,编程,Python",
    )


class TestModerationGraph:
    def test_pre_screen_clean(self):
        from graphs.moderation_graph import pre_screen
        state = {
            "content_type": "video", "content_id": 1,
            "title": "Python教程", "description": "学习编程", "tags": "教程",
            "content": "",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": "", "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = pre_screen(state)
        assert result["pre_screen_matched"] is False

    def test_pre_screen_spam(self):
        from graphs.moderation_graph import pre_screen
        state = {
            "content_type": "comment", "content_id": 2,
            "title": "", "description": "", "tags": "",
            "content": "加微信xxx，日赚千元，免费领取",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": "", "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = pre_screen(state)
        assert result["pre_screen_matched"] is True
        assert "spam" in result["pre_screen_categories"]

    def test_decide_action_safe(self):
        from graphs.moderation_graph import decide_action
        state = {
            "content_type": "video", "content_id": 1,
            "title": "", "description": "", "tags": "", "content": "",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": '{"is_safe": true, "confidence": 0.95, "risk_categories": [], "review_note": ""}',
            "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = decide_action(state)
        assert result["result"] == "approved"

    def test_decide_action_unsafe(self):
        from graphs.moderation_graph import decide_action
        state = {
            "content_type": "video", "content_id": 1,
            "title": "", "description": "", "tags": "", "content": "",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": '{"is_safe": false, "confidence": 0.85, "risk_categories": ["spam"], "review_note": "包含广告"}',
            "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = decide_action(state)
        assert result["result"] == "rejected"

    def test_decide_action_pending(self):
        from graphs.moderation_graph import decide_action
        state = {
            "content_type": "video", "content_id": 1,
            "title": "", "description": "", "tags": "", "content": "",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": '{"is_safe": false, "confidence": 0.5, "risk_categories": [], "review_note": "不确定"}',
            "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = decide_action(state)
        assert result["result"] == "pending"

    def test_decide_action_bad_json(self):
        from graphs.moderation_graph import decide_action
        state = {
            "content_type": "video", "content_id": 1,
            "title": "", "description": "", "tags": "", "content": "",
            "pre_screen_matched": False, "pre_screen_categories": [],
            "llm_result_raw": "not valid json at all",
            "result": "", "confidence": 0.0,
            "risk_categories": [], "review_note": "",
        }
        result = decide_action(state)
        assert result["result"] == "pending"


class TestModerationAgent:
    @pytest.mark.asyncio
    async def test_full_graph(self, video_request):
        from agents.moderation_agent import moderation_agent
        with patch("graphs.moderation_graph.call_llm", new_callable=AsyncMock) as mock_llm:
            mock_llm.return_value = '{"is_safe": true, "confidence": 0.95, "risk_categories": [], "review_note": ""}'
            result = await moderation_agent.moderate(video_request)
            assert result.result == "approved"


class TestModerationAPI:
    def test_health(self, client):
        resp = client.get("/api/ai/health")
        assert resp.status_code == 200
        assert resp.json()["status"] == "success"

    def test_moderate_video(self, client):
        from agents import moderation_agent as ma
        mock_result = ModerationResponse(
            content_type=ContentType.VIDEO, content_id=1,
            result="approved", confidence=0.95,
            risk_categories=[], review_note="ok"
        )
        async def fake_moderate(self, req):
            return mock_result

        with patch.object(ma.moderation_agent.__class__, "moderate", fake_moderate):
            resp = client.post("/api/ai/moderation/video", json={
                "content_type": "video", "content_id": 1,
                "title": "测试", "description": "测试", "tags": "测试"
            })
            assert resp.status_code == 200
            assert resp.json()["status"] == "success"
