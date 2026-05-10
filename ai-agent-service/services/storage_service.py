from datetime import datetime

import pymysql

from config import settings
from models.schemas import ModerationLog


class StorageService:
    """审核日志持久化服务。"""

    def _get_conn(self):
        return pymysql.connect(
            host=settings.mysql_host,
            user=settings.mysql_user,
            password=settings.mysql_password,
            database=settings.mysql_database,
            charset="utf8mb4",
            cursorclass=pymysql.cursors.DictCursor,
        )

    def save_moderation_log(self, log: ModerationLog) -> int:
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    """INSERT INTO moderation_log
                       (content_type, content_id, result, confidence, risk_categories, review_note, reviewed_by, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
                    (
                        log.content_type,
                        log.content_id,
                        log.result,
                        log.confidence,
                        log.risk_categories,
                        log.review_note,
                        log.reviewed_by,
                        datetime.now(),
                    ),
                )
                conn.commit()
                return cur.lastrowid
        finally:
            conn.close()

    def get_moderation_log(self, content_type: str, content_id: int) -> dict | None:
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    "SELECT * FROM moderation_log WHERE content_type=%s AND content_id=%s ORDER BY created_at DESC LIMIT 1",
                    (content_type, content_id),
                )
                return cur.fetchone()
        finally:
            conn.close()

    def create_table_if_not_exists(self):
        """建表（如果尚不存在）。"""
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    """CREATE TABLE IF NOT EXISTS moderation_log (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        content_type VARCHAR(20) NOT NULL COMMENT 'video/comment/danmaku',
                        content_id INT NOT NULL,
                        result VARCHAR(20) NOT NULL COMMENT 'approved/rejected/pending',
                        confidence DECIMAL(4,3),
                        risk_categories VARCHAR(255) COMMENT 'risk categories',
                        review_note TEXT COMMENT 'AI review note',
                        reviewed_by VARCHAR(50) DEFAULT 'AI',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_content (content_type, content_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"""
                )
                conn.commit()
        finally:
            conn.close()


    def get_all_tags(self) -> list[str]:
        """从 video 表提取所有已有的标签词汇。"""
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT DISTINCT tags FROM video WHERE tags IS NOT NULL AND tags != ''")
                rows = cur.fetchall()
        finally:
            conn.close()

        all_tags: set[str] = set()
        for row in rows:
            tags_str = row.get("tags", "")
            for tag in tags_str.split(","):
                tag = tag.strip()
                if tag:
                    all_tags.add(tag)
        return list(all_tags)


# Singleton
storage_service = StorageService()
