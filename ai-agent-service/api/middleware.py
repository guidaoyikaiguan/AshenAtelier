import jwt
from fastapi import Header, HTTPException, Request
from config import settings


async def verify_jwt(request: Request):
    """验证 JWT token（对内 API 可选，对外暴露给前端的接口使用）。"""
    # 只在需要鉴权的路由上使用，不是全局中间件
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing or invalid token")
    token = auth_header[7:]
    try:
        payload = jwt.decode(token, settings.jwt_secret_key, algorithms=["HS256"])
        request.state.user_id = payload.get("userId")
        request.state.email = payload.get("email")
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid token")
