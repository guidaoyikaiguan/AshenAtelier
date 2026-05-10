import logging
import asyncio
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from api.routes import router
from config import settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Pre-warm ASR model in background (non-blocking)
    from services.asr_service import asr_service
    loop = asyncio.get_event_loop()
    task = loop.run_in_executor(None, lambda: asr_service.model)
    yield


app = FastAPI(title="Shipin AI Agent Service", version="0.1.0", lifespan=lifespan)

app.include_router(router)

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.server_host,
        port=settings.server_port,
        reload=False,
    )
