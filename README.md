# 魔女之旅 · 萤石驿站

> 一个以《魔女之旅》伊蕾娜为主题的二次元视频分享平台。灰之魔女将化身为看板娘桌宠，陪伴每一位旅人浏览视频、畅聊日常。

**魔女之旅** 是一个全栈视频社区，不只是播放器——它融合了 AI 看板娘桌宠、弹幕系统、实时聊天、动态分享、智能推荐，用微服务架构把这一切串在一起。前端是 Vue 3 驱动的魔法结界，后端用 Spring Boot 微服务和 Python AI 服务撑起整个魔女工坊。

---

## 项目亮点

### 伊蕾娜看板娘桌宠
她会站在页面右下角，拖拽移动，点击就能聊天。
- **大模型对话**：接入了 DeepSeek，用 SSE 流式输出回复，带打字机效果
- **情绪表现**：7 种表情（开心/生气/难过/惊讶/害羞/思考/默认），根据对话内容自动切换
- **日语语音**：回复内容附带日语翻译，通过 GPT-SoVITS 实时合成语音，与文字同步播放
- **语音输入**：支持浏览器录音 → ASR 转文字 → 自动发送
- **视频感知**：在视频详情页，伊蕾娜知道你在看什么，能陪你聊视频内容
- **记忆功能**：基于 Redis 的会话记忆，刷新页面后还能接着聊（7 天有效）

### AI 内容审核
上传视频、发弹幕、写评论——所有 UGC 内容自动过 AI 审核，不合规的直接拦截或标记。

### 弹幕系统
兼容 DPlayer v3 协议的实时弹幕，AI 自动审核每条弹幕，视频作者可以管理自己视频的弹幕。

### 实时通讯
WebSocket 驱动的用户间私聊 + AI 智能助手，消息即时推送，在线状态可见。

### 微服务架构
10 个 Docker 服务协同工作：MySQL + Redis + RabbitMQ + Elasticsearch + Eureka 注册中心 + Spring Cloud Gateway + 推荐引擎 + Python AI 服务 + Spring Boot 后端 + Vue 前端。

### 社区功能
动态发布（支持七牛云图片上传）、点赞评论、关注系统、收藏夹、播放历史、视频合集。

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API) · Element Plus · Pinia · Vue Router · DPlayer · Axios |
| 后端 | Spring Boot 2.x · MyBatis · Spring Cloud Gateway · NetFlix Eureka |
| AI 服务 | Python FastAPI · DeepSeek LLM · Faster-Whisper (ASR) · GPT-SoVITS (TTS) · LangGraph |
| 数据库 | MySQL 8.0 · Redis 7 · Elasticsearch 8.14 |
| 消息队列 | RabbitMQ 3.12 |
| 存储 | 七牛云 Kodo 对象存储 |
| 部署 | Docker Compose · Nginx · Docker 多阶段构建 |

---

## 项目结构

```
AshenAtelier/
├── ceshi/                      # Vue 3 前端
│   ├── src/
│   │   ├── components/         # 组件（看板娘、Header、Sidebar 等）
│   │   ├── views/              # 页面（首页/分区/视频/动态/聊天/个人中心/后台）
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── api/                # API 接口定义
│   ├── public/                 # 静态资源（轮播图/背景/看板娘表情）
│   └── nginx.conf              # 前端 Nginx 配置
│
├── shipin/                     # Spring Boot 主后端
│   └── src/main/java/com/shipin/
│       ├── controller/         # 控制器（web/admin）
│       ├── service/            # 业务层
│       ├── entity/             # 实体/查询/VO
│       ├── mappers/            # MyBatis 映射
│       ├── config/             # Spring 配置
│       ├── websocket/          # WebSocket 实时聊天
│       └── utils/              # JWT/Redis/密码等工具
│
├── ai-agent-service/           # Python AI 智能体
│   ├── agents/                 # AI Agent（看板娘/客服/审核/标签）
│   ├── graphs/                 # LangGraph 对话流程
│   ├── prompts/                # 提示词模板
│   ├── services/               # LLM/ASR/TTS/存储服务
│   └── api/                    # FastAPI 路由
│
├── api-gateway/                # Spring Cloud Gateway 网关
├── eureka-server/              # Eureka 服务注册中心
├── recommend-service/          # 视频推荐微服务
├── docker/                     # Docker 初始化脚本（MySQL 建表）
└── docker-compose.yml          # 一键部署编排文件
```

---

## 快速开始

### 前提条件

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)（含 Docker Compose）
- 至少 8GB 可用内存（10 个服务同时运行较吃资源）
- 七牛云账号（用于视频/图片上传）
- DeepSeek API Key（用于 AI 看板娘和内容审核）

### 1. 克隆仓库

```bash
git clone https://github.com/guidaoyikaiguan/AshenAtelier.git
cd AshenAtelier
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env
```

然后编辑 `.env` 文件，填入你的实际密钥：

```bash
# ---------- 必填 ----------
QINIU_ACCESS_KEY=你的七牛云AccessKey
QINIU_SECRET_KEY=你的七牛云SecretKey
QINIU_BUCKET_NAME=你的存储空间名
QINIU_DOMAIN=你的CDN域名

LLM_API_KEY=你的DeepSeek_API_Key

JWT_SECRET_KEY=随便一个长字符串，至少32位

# ---------- 可选 ----------
MYSQL_ROOT_PASSWORD=root              # 本地开发不用改
RABBITMQ_PASSWORD=guest               # 本地开发不用改
```

### 3. 启动所有服务

```bash
docker compose up -d
```

首次启动会自动拉取基础镜像并构建所有服务的 Docker 镜像，大约需要 5-10 分钟（取决于网络和机器性能）。

### 4. 检查服务状态

```bash
docker compose ps
```

确保所有 10 个服务都是 `healthy` 或 `running` 状态。关键端口：

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端页面 | `http://localhost` | Vue SPA 主站 |
| API 网关 | `8080` | 所有 API 的统一入口 |
| 后端服务 | `8081` | Spring Boot 主服务 |
| AI 服务 | `8084` | Python AI 智能体 |
| Eureka 面板 | `8761` | 服务注册中心监控 |

打开浏览器访问 `http://localhost`，看到伊蕾娜在右下角跟你打招呼就说明启动成功了。

### 5. 更新部署

当你修改了代码想重新部署时：

```bash
# 重新构建并重启指定服务
docker compose build <服务名>
docker compose up -d <服务名>

# 例如：改完前端代码后
docker compose build frontend
docker compose up -d frontend

# 改完 AI 服务后
docker compose build ai-agent-service
docker compose up -d ai-agent-service
```

---

## 常见问题

**Q: 某个服务起不来，日志里有什么？**
```bash
docker compose logs <服务名>
# 例如：docker compose logs ai-agent-service
```

**Q: 数据库表没创建？**
初始化 SQL 在 `docker/mysql/init/` 目录，MySQL 容器首次启动会自动执行。如果之前启动过想重来，删掉数据卷：
```bash
docker compose down -v
docker compose up -d
```

**Q: 看板娘不说话？**
在ai-agent-service/api/routes.py中的第 242-243 行：

  TTS_API_URL = "http://127.0.0.1:9880/tts"
  TTS_REF_DIR = "D:/tts_ref"
  
其中TTS_API_URL 要求电脑本地有正在运行的GPT-SoVITS服务，TTS_REF_DIR 是存放音频的本地文件夹，想要使用语音功能需要将TTS_REF_DIR中的路径改成自己电脑中音频文件的路径
修改后需先停掉docker中的python ai服务改为本地启动python ai服务
**Q: 视频上传失败？**
检查七牛云的四个环境变量是否都配对了，以及存储空间是否创建。

**Q: Windows 下端口被占用？**
80 端口可能被 IIS 或其他服务占用，可以修改 `docker-compose.yml` 中 `frontend` 的端口映射，比如改成 `8088:80`。

---

## License

MIT License · 详见 [LICENSE](LICENSE)
