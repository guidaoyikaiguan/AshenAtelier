import json
import logging

from config import settings

logger = logging.getLogger(__name__)

TTL = 604800       # 7 days
MAX_MESSAGES = 50
KEY_PREFIX = "mascot:history:"


class MascotHistoryService:
    _redis = None

    def _get_redis(self):
        if self._redis is None:
            import redis.asyncio as aioredis
            self._redis = aioredis.from_url(
                f"redis://{settings.redis_host}:{settings.redis_port}",
                decode_responses=True,
            )
        return self._redis

    async def get_history(self, mascot_id: str) -> list[dict]:
        try:
            r = self._get_redis()
            raw = await r.get(f"{KEY_PREFIX}{mascot_id}")
            if raw:
                return json.loads(raw)
        except Exception:
            logger.warning("Failed to load mascot history for %s", mascot_id, exc_info=True)
        return []

    async def save_history(self, mascot_id: str, history: list[dict]) -> None:
        try:
            r = self._get_redis()
            truncated = history[-MAX_MESSAGES:]
            await r.setex(
                f"{KEY_PREFIX}{mascot_id}",
                TTL,
                json.dumps(truncated, ensure_ascii=False),
            )
        except Exception:
            logger.warning("Failed to save mascot history for %s", mascot_id, exc_info=True)


mascot_history_service = MascotHistoryService()
