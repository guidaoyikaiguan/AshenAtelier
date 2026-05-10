/*
  管理员用户表
*/
CREATE TABLE `admin` (
  `admin_id` int(11) NOT NULL,
  `admin_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  轮播图表
*/
CREATE TABLE `carousel` (
  `carousel_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `category_id` int(11) NOT NULL COMMENT '分类ID',
  `title` varchar(255) NOT NULL COMMENT '轮播图标题',
  `cover` varchar(500) NOT NULL COMMENT '轮播图图片URL（七牛云存储）',
  `link` varchar(500) DEFAULT NULL COMMENT '跳转链接',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序权重',
  `status` int(11) DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`carousel_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='轮播图表';
/*
  视频分区表
*/
CREATE TABLE `category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序权重',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频分类表';
/*
  聊天数据表
*/
CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`),
  CONSTRAINT `chat_message_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chat_message_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*
  记录聊天表
*/
CREATE TABLE `chat_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id1` int(11) NOT NULL,
  `user_id2` int(11) NOT NULL,
  `last_message_id` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_session` (`user_id1`,`user_id2`),
  KEY `user_id2` (`user_id2`),
  KEY `last_message_id` (`last_message_id`),
  CONSTRAINT `chat_session_ibfk_1` FOREIGN KEY (`user_id1`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chat_session_ibfk_2` FOREIGN KEY (`user_id2`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chat_session_ibfk_3` FOREIGN KEY (`last_message_id`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*
  收藏表
*/
CREATE TABLE `collect` (
  `user_id` int(11) DEFAULT NULL,
  `video_id` int(11) DEFAULT NULL,
  `favorite` int(255) DEFAULT NULL,
  `favorite_name` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `is_public` int(255) DEFAULT NULL COMMENT '公开是1，私有是0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  评论表
*/
CREATE TABLE `comment` (
  `comment_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `video_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `comment_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `parent_id` int(11) DEFAULT NULL,
  `like_count` int(11) NOT NULL DEFAULT '0',
  `if_top` int(255) DEFAULT NULL COMMENT '是否置顶',
  PRIMARY KEY (`comment_id`),
  KEY `idx_comment_video_id` (`video_id`),
  KEY `idx_comment_user_id` (`user_id`),
  KEY `idx_comment_parent_id` (`parent_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*
  评论点赞表
*/
CREATE TABLE `comment_like` (
  `user_id` int(11) NOT NULL,
  `comment_id` int(11) NOT NULL,
  `like_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`comment_id`),
  KEY `comment_id` (`comment_id`),
  CONSTRAINT `comment_like_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `comment_like_ibfk_2` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  合集表
*/
CREATE TABLE `compilations` (
  `user_id` int(11) DEFAULT NULL,
  `video_id` int(11) DEFAULT NULL,
  `compilations_id` int(11) DEFAULT NULL,
  `compilations_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  弹幕表
*/
CREATE TABLE `danmaku` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `video_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `content` varchar(255) NOT NULL,
  `time` float NOT NULL,
  `color` varchar(10) DEFAULT '#ffffff',
  `type` int(11) DEFAULT '1',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `video_id` (`video_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `danmaku_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`),
  CONSTRAINT `danmaku_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;
/*
  关注粉丝表
*/
CREATE TABLE `fans` (
  `user_id` int(11) DEFAULT NULL,
  `attention_id` int(11) DEFAULT NULL,
  `fan_id` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `clicks` int(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  动态表
*/
CREATE TABLE `moment` (
  `moment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `image_count` tinyint(3) unsigned DEFAULT '0',
  `like_count` int(10) unsigned DEFAULT '0',
  `comment_count` int(10) unsigned DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`moment_id`),
  KEY `idx_user_create` (`user_id`,`create_time`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `moment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*
  动态评论表
*/
CREATE TABLE `moment_comment` (
  `comment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `moment_id` int(10) unsigned NOT NULL,
  `parent_id` int(10) unsigned DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `like_count` int(10) unsigned DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `user_id` (`user_id`),
  KEY `parent_id` (`parent_id`),
  KEY `idx_moment_create` (`moment_id`,`create_time`),
  CONSTRAINT `moment_comment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `moment_comment_ibfk_2` FOREIGN KEY (`moment_id`) REFERENCES `moment` (`moment_id`) ON DELETE CASCADE,
  CONSTRAINT `moment_comment_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `moment_comment` (`comment_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*
  动态评论点赞表
*/
CREATE TABLE `moment_comment_like` (
  `user_id` int(11) NOT NULL,
  `moment_comment_id` int(11) NOT NULL,
  `like_time` datetime NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  动态图片内容表
*/
CREATE TABLE `moment_image` (
  `image_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `moment_id` int(10) unsigned NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `sort_order` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_moment_id` (`moment_id`),
  CONSTRAINT `moment_image_ibfk_1` FOREIGN KEY (`moment_id`) REFERENCES `moment` (`moment_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*
  动态点赞表
*/
CREATE TABLE `moment_like` (
  `like_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `moment_id` int(10) unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `idx_user_moment` (`user_id`,`moment_id`),
  KEY `moment_id` (`moment_id`),
  CONSTRAINT `moment_like_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `moment_like_ibfk_2` FOREIGN KEY (`moment_id`) REFERENCES `moment` (`moment_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*
  用户表
*/
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `nick_name` varchar(255) DEFAULT NULL,
  `password` varchar(32) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `my_signature` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `my_coin` int(255) DEFAULT NULL,
  `gender` int(11) DEFAULT NULL,
  `my_Announcement` varchar(255) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL COMMENT '上次登录时间',
  `state` int(11) DEFAULT '1' COMMENT '用户状态：1-正常，2-封禁，0-未激活',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*
  检查用户观看操作表
*/
CREATE TABLE `usersee` (
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `video_id` int(11) DEFAULT NULL COMMENT '视频id',
  `iflike` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否点赞',
  `ifcollect` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否收藏',
  `insertcoins` int(255) DEFAULT NULL COMMENT '投币'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*
  视频表
*/
CREATE TABLE `video` (
  `video_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `user_id` int(11) DEFAULT NULL COMMENT '上传用户ID',
  `categroy_id` int(11) DEFAULT NULL COMMENT '视频分类ID',
  `title` varchar(255) DEFAULT NULL COMMENT '视频标题',
  `description` varchar(255) DEFAULT NULL COMMENT '视频描述',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频存储路径',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '视频封面图路径',
  `duration` varchar(255) DEFAULT NULL COMMENT '视频时长（秒）',
  `play_count` varchar(255) DEFAULT NULL COMMENT '播放量',
  `like_count` varchar(255) DEFAULT NULL COMMENT '点赞数',
  `comment_count` varchar(255) DEFAULT NULL COMMENT '评论数',
  `status` varchar(255) DEFAULT NULL COMMENT '状态（0：待审核，1：已通过，2：已拒绝）',
  `create_time` datetime DEFAULT NULL COMMENT '上传时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `coins_inserted` varchar(255) DEFAULT NULL COMMENT '硬币数',
  `follow_count` varchar(255) DEFAULT NULL COMMENT '收藏数',
  `tags` varchar(255) DEFAULT NULL COMMENT '视频标签，多个标签用逗号分隔',
  PRIMARY KEY (`video_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*
  视频历史表
*/
CREATE TABLE `video_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `video_id` bigint(20) NOT NULL COMMENT '视频ID',
  `progress` int(11) NOT NULL DEFAULT '0' COMMENT '观看进度（秒）',
  `last_watch_time` datetime NOT NULL COMMENT '最后观看时间',
  `watch_duration` int(11) NOT NULL DEFAULT '0' COMMENT '观看时长（秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_video` (`user_id`,`video_id`) COMMENT '确保用户对每个视频只有一条记录',
  KEY `idx_user_lastwatch` (`user_id`,`last_watch_time` DESC) COMMENT '用户ID+最后观看时间，加速最近观看查询',
  KEY `idx_video_id` (`video_id`) COMMENT '视频ID索引，加速查询视频观看情况'
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频观看历史记录';