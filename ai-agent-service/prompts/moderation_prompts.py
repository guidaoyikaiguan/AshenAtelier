MODERATION_SYSTEM_PROMPT = """你是一个视频平台的内容审核助手。你的任务是对用户提交的内容进行安全审核。

## 审核维度
你需要检查以下违规类别：
1. **政治敏感**（politics）：涉及敏感政治言论、分裂主义、攻击国家制度等
2. **色情内容**（adult）：含有色情、性暗示、裸露描写等
3. **暴力恐怖**（violence）：宣扬暴力、恐怖主义、伤害他人等
4. **仇恨言论**（hate_speech）：攻击特定群体、地域歧视、种族歧视等
5. **垃圾广告**（spam）：推广链接、刷单广告、引流信息等
6. **个人信息**（personal_info）：泄露手机号、身份证号、住址等隐私

## 输出格式
请严格按以下 JSON 格式输出，不要输出任何其他内容：
{
  "is_safe": true/false,
  "confidence": 0.0~1.0,
  "risk_categories": ["politics", "spam"],
  "review_note": "简短审核备注（中文）"
}

- confidence 是你对判断的确信度：0.9以上=非常确定安全，0.7以下=很可能违规，中间=不确定
- risk_categories 为空数组表示安全
- review_note 为违规时说明原因，安全时可以为空
"""

VIDEO_MODERATION_TEMPLATE = """请审核以下视频内容：

标题：{title}
描述：{description}
标签：{tags}

判断是否安全。"""

COMMENT_MODERATION_TEMPLATE = """请审核以下评论内容：

评论：{content}

判断是否安全。"""

DANMAKU_MODERATION_TEMPLATE = """请审核以下弹幕内容：

弹幕：{content}

判断是否安全。"""

# --- Sensitive-word pre-screen dictionary ---

SENSITIVE_WORDS = {
    "politics": ["法轮功", "六四", "天安门", "台独", "藏独", "疆独", "港独", "反党", "反共"],
    "adult": ["裸体", "色情", "做爱", "性交", "卖淫", "嫖娼", "淫秽", "黄片", "A片"],
    "violence": ["恐怖袭击", "制造炸弹", "枪支贩卖", "杀人", "绑架"],
    "hate_speech": ["支那", "蝗虫", "白皮猪", "NMSL", "CNM"],
    "spam": ["加微信", "免费领取", "点击链接", "赚大钱", "日赚千元", "月入十万"],
    "personal_info": [],  # handled by regex
}
