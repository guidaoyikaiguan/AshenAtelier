import httpx

from config import settings


class ShipinClient:
    """HTTP 客户端，调用 Java shipin 后端 API。"""

    def __init__(self):
        self.base_url = settings.shipin_service_url.rstrip("/")

    async def _post(self, path: str, json_data: dict) -> dict:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.post(f"{self.base_url}{path}", json=json_data)
            resp.raise_for_status()
            return resp.json()

    async def _get(self, path: str, params: dict | None = None) -> dict:
        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.get(f"{self.base_url}{path}", params=params)
            resp.raise_for_status()
            return resp.json()

    async def update_video_status(self, video_id: int, status: str, review_note: str = "") -> dict:
        """更新视频审核状态。调用 Java VideoController.updateVideoStatus()。"""
        data = {"videoId": video_id, "status": status}
        if review_note:
            data["review_note"] = review_note
        return await self._post("/api/video/updateVideoStatus", data)

    async def update_video_tags(self, video_id: int, tags: str) -> dict:
        """更新视频标签。调用 Java VideoController.updateVideoTags()。"""
        return await self._post("/api/video/updateVideoTags", {"videoId": video_id, "tags": tags})

    async def get_video(self, video_id: int) -> dict:
        """获取视频详情。"""
        return await self._get(f"/api/video/loadVideo/{video_id}")

    async def get_all_videos(self) -> list[dict]:
        """获取所有视频（用于标签补充）。"""
        result = await self._get("/api/video/loadAllVideo")
        return result.get("data", {}).get("list", [])


# Singleton
shipin_client = ShipinClient()
