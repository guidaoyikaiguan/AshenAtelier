-- ============================================================
-- AI Agent Service 数据库迁移
-- 注意：此脚本在 01_shipin_schema.sql 之后执行，需要先 USE shipin
-- ============================================================

USE `shipin`;

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

-- 2. Video 表增加审核备注字段（忽略已存在的错误）
-- MySQL 不支持 ADD COLUMN IF NOT EXISTS，用存储过程模拟
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def  VARCHAR(256)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'shipin'
          AND TABLE_NAME = tbl_name
          AND COLUMN_NAME = col_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists('video', 'review_note', 'VARCHAR(500) DEFAULT NULL');
CALL add_column_if_not_exists('comment', 'review_status', "VARCHAR(20) DEFAULT 'approved' COMMENT 'approved/rejected/pending'");
CALL add_column_if_not_exists('danmaku', 'review_status', "VARCHAR(20) DEFAULT 'approved' COMMENT 'approved/rejected/pending'");

DROP PROCEDURE IF EXISTS add_column_if_not_exists;
