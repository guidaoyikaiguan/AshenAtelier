import pytest
from unittest.mock import AsyncMock, patch

from models.schemas import ChatRequest, ChatResponse


class TestCustomerServiceGraph:
    def test_check_escalation(self):
        from graphs.customer_service_graph import check_escalation
        state = {
            "user_id": 1, "message": "???", "llm_result_raw": "",
            "intent": "general", "reply": "不确定的回复",
            "confidence": 0.3, "should_escalate": False,
        }
        result = check_escalation(state)
        assert result["should_escalate"] is True
        assert "人工客服" in result["reply"]


class TestCustomerServiceAgent:
    @pytest.mark.asyncio
    async def test_chat_help(self):
        from agents.customer_service_agent import customer_service_agent
        with patch("graphs.customer_service_graph.call_llm", new_callable=AsyncMock) as mock_llm:
            mock_llm.return_value = (
                '{"intent": "upload_help", '
                '"reply": "您可以点击右上角的投稿按钮来上传视频。", '
                '"confidence": 0.95, "should_escalate": false}'
            )
            req = ChatRequest(user_id=1, message="怎么上传视频？")
            result = await customer_service_agent.chat(req)
            assert result.intent == "upload_help"
            assert len(result.reply) > 0

    @pytest.mark.asyncio
    async def test_chat_escalate(self):
        from agents.customer_service_agent import customer_service_agent
        with patch("graphs.customer_service_graph.call_llm", new_callable=AsyncMock) as mock_llm:
            mock_llm.return_value = (
                '{"intent": "general", '
                '"reply": "抱歉我不太确定。", '
                '"confidence": 0.3, "should_escalate": true}'
            )
            req = ChatRequest(user_id=1, message="今天天气怎么样？")
            result = await customer_service_agent.chat(req)
            assert result.should_escalate is True


class TestCustomerServiceAPI:
    def test_chat_endpoint(self, client):
        from agents import customer_service_agent as csa
        mock_resp = ChatResponse(reply="测试回复", intent="general", confidence=0.9, should_escalate=False)
        async def fake_chat(self, req):
            return mock_resp

        with patch.object(csa.customer_service_agent.__class__, "chat", fake_chat):
            resp = client.post("/api/ai/chat/message", json={
                "user_id": 1, "message": "你好"
            })
            assert resp.status_code == 200
            assert resp.json()["status"] == "success"
