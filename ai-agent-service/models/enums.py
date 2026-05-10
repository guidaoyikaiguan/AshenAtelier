from enum import Enum


class ContentType(str, Enum):
    VIDEO = "video"
    COMMENT = "comment"
    DANMAKU = "danmaku"


class ModerationResult(str, Enum):
    APPROVED = "approved"
    REJECTED = "rejected"
    PENDING = "pending"


class IntentCategory(str, Enum):
    UPLOAD_HELP = "upload_help"
    ACCOUNT_ISSUE = "account_issue"
    FEATURE_QUESTION = "feature_question"
    CONTENT_POLICY = "content_policy"
    GENERAL = "general"
