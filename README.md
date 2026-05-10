# AshenAtelier (shipinbofang)

一个基于 Spring Boot + Vue 3 的视频分享与社区平台，支持视频上传播放、动态发布、实时聊天等功能。

## 技术栈

**后端:**
- Java Spring Boot 2.x
- MyBatis + MySQL
- Redis（缓存/分布式锁）
- RabbitMQ（消息队列）
- Elasticsearch（全文搜索）
- JWT 认证
- 七牛云对象存储

**前端:**
- Vue 3（Composition API）
- Element Plus UI
- Vue Router + Pinia
- Axios

**AI 服务:**
- Python FastAPI
- DeepSeek LLM 集成
- 语音识别 (ASR) / 语音合成 (TTS)

**基础设施:**
- Docker Compose 一键部署
- Nginx 反向代理
- Spring Cloud Gateway

## 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/guidaoyikaiguan/AshenAtelier.git
cd AshenAtelier

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入你的 API Key 等配置

# 3. 启动所有服务
docker-compose up -d
```

## 项目结构

```
├── shipin/              # Spring Boot 后端
├── ceshi/               # Vue 3 前端
├── ai-agent-service/    # Python AI 服务
├── api-gateway/         # Spring Cloud Gateway
├── eureka-server/       # 服务注册中心
├── recommend-service/   # 推荐服务
├── nginx.conf           # Nginx 配置
└── docker-compose.yml   # Docker 编排
```

## License

MIT License - 详见 [LICENSE](LICENSE)
