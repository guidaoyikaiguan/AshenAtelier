import os

from pydantic_settings import BaseSettings

_env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")


class Settings(BaseSettings):
    # LLM
    llm_provider: str = "deepseek"
    llm_model: str = "deepseek-chat"
    llm_api_key: str = ""
    llm_base_url: str = "https://api.deepseek.com/v1"

    # Redis
    redis_host: str = "localhost"
    redis_port: int = 6379

    # MySQL
    mysql_host: str = "localhost"
    mysql_user: str = "root"
    mysql_password: str = "root"
    mysql_database: str = "shipin"

    # Shipin backend
    shipin_service_url: str = "http://localhost:8081"

    # JWT
    jwt_secret_key: str = "abcdefghijklmnopqrstuvwxyz1234567890abcdef"

    # Moderation thresholds
    moderation_auto_approve_threshold: float = 0.9
    moderation_flag_threshold: float = 0.7
    moderation_auto_reject_threshold: float = 0.5

    # Server
    server_host: str = "0.0.0.0"
    server_port: int = 8084

    model_config = {"env_file": _env_path, "env_file_encoding": "utf-8"}


settings = Settings()
