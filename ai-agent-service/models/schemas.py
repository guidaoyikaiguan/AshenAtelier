from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field

from .enums import ContentType, IntentCategory, ModerationResult


# --- Unified Response (matches Java Result<T>) ---

class ApiResponse(BaseModel):
    status: str = "success"
    code: int = 200
    msg: str = "success"
    data: Optional[dict] = None

    @classmethod
    def ok(cls, data: dict | None = None, msg: str = "success") -> "ApiResponse":
        return cls(status="success", code=200, msg=msg, data=data)

    @classmethod
    def fail(cls, code: int = 500, msg: str = "error") -> "ApiResponse":
        return cls(status="error", code=code, msg=msg)


# --- Moderation ---

class ModerationRequest(BaseModel):
    content_type: ContentType = Field(description="video / comment / danmaku")
    content_id: int
    title: Optional[str] = None
    description: Optional[str] = None
    tags: Optional[str] = None
    content: Optional[str] = None  # comment or danmaku text


class ModerationResponse(BaseModel):
    content_type: ContentType
    content_id: int
    result: ModerationResult
    confidence: float
    risk_categories: list[str] = []
    review_note: str = ""


class ModerationLog(BaseModel):
    id: Optional[int] = None
    content_type: ContentType
    content_id: int
    result: ModerationResult
    confidence: float
    risk_categories: Optional[str] = None
    review_note: Optional[str] = None
    reviewed_by: str = "AI"
    created_at: Optional[datetime] = None


# --- Tagging ---

class TagSuggestRequest(BaseModel):
    title: str
    description: Optional[str] = ""


class TagSuggestion(BaseModel):
    tag: str
    relevance: float


class TagSuggestResponse(BaseModel):
    suggestions: list[TagSuggestion]


class TagEnrichRequest(BaseModel):
    video_id: int
    title: str
    description: Optional[str] = ""
    existing_tags: Optional[str] = ""


class TagEnrichResponse(BaseModel):
    video_id: int
    suggested_tags: list[str]
    merged_tags: str  # comma-separated final tag string


# --- Customer Service ---

class ChatRequest(BaseModel):
    user_id: int
    message: str


class ChatResponse(BaseModel):
    reply: str
    intent: IntentCategory = IntentCategory.GENERAL
    confidence: float = 1.0
    should_escalate: bool = False


# --- Mascot ---

class MascotChatRequest(BaseModel):
    message: str
    conversation_history: list[dict] = []
    video_context: Optional[dict] = None  # { videoId, title, description, tags }
    transcript: Optional[str] = None  # video audio transcription


class MascotChatResponse(BaseModel):
    mood: str
    reply: str
    reply_ja: str = ""


class MascotHistoryRequest(BaseModel):
    mascot_id: str = Field(..., min_length=1, max_length=128)
    history: list[dict] = Field(default=[])
