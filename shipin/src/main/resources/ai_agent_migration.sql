-- ============================================================
-- AI Agent Service 数据库迁移脚本
-- 在 shipin 数据库中执行
-- ============================================================

-- 1. 新增审核日志表
CREATE TABLE IF NOT EXISTS moderation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content_type VARCHAR(20) NOT NULL COMMENT 'video/comment/danmaku',
    content_id INT NOT NULL,
    result VARCHAR(20) NOT NULL COMMENT 'approved/rejected/pending',
    confidence DECIMAL(4,3),
    risk_categories VARCHAR(255) COMMENT '命中的风险类别',
    review_note TEXT COMMENT 'AI审核备注',
    reviewed_by VARCHAR(50) DEFAULT 'AI',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content (content_type, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Video 表增加审核备注字段（如果尚不存在）
ALTER TABLE video ADD COLUMN IF NOT EXISTS review_note VARCHAR(500) DEFAULT NULL;

-- 3. Comment 表增加审核状态字段（如果尚不存在）
ALTER TABLE comment ADD COLUMN IF NOT EXISTS review_status VARCHAR(20) DEFAULT 'approved' COMMENT 'approved/rejected/pending';

-- 4. Danmaku 表增加审核状态字段（如果尚不存在）
ALTER TABLE danmaku ADD COLUMN IF NOT EXISTS review_status VARCHAR(20) DEFAULT 'approved' COMMENT 'approved/rejected/pending';
