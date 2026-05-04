-- ============================================================
-- BookNexus 图书管理系统 — 数据库初始化脚本
-- 包含：DDL（12 张业务表 + 索引）+ 种子数据
-- 数据库名：booknexus  字符集：utf8mb4  引擎：InnoDB
-- @author 张俊文  @since 2026-04-30
-- ============================================================

CREATE DATABASE IF NOT EXISTS booknexus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE booknexus;

-- ============================================================
-- 1. user — 用户表（含角色字段区分管理员/普通用户）
-- ============================================================
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 加密）',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN / USER',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED / DISABLED',
    `avatar_url`    VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_username` (`username`) USING BTREE,
    UNIQUE KEY `uk_user_email` (`email`) USING BTREE,
    KEY `idx_user_status` (`status`) USING BTREE,
    KEY `idx_user_role` (`role`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. book — 书籍表
-- ============================================================
CREATE TABLE `book` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '书名',
    `author`          VARCHAR(100) NOT NULL COMMENT '作者',
    `isbn`            VARCHAR(20)  DEFAULT NULL COMMENT 'ISBN号',
    `publisher`       VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `publish_date`    DATE         DEFAULT NULL COMMENT '出版日期',
    `description`     TEXT         DEFAULT NULL COMMENT '书籍简介',
    `cover_url`       VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL（MinIO）',
    `stock`           INT          NOT NULL DEFAULT 1 COMMENT '总库存量',
    `available_stock` INT          NOT NULL DEFAULT 1 COMMENT '可借库存量',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE/BORROWED/DAMAGED/LOST',
    `bookshelf_id`    BIGINT       DEFAULT NULL COMMENT '书架ID',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_book_isbn` (`isbn`) USING BTREE,
    KEY `idx_book_title` (`title`) USING BTREE,
    KEY `idx_book_author` (`author`) USING BTREE,
    KEY `idx_book_status` (`status`) USING BTREE,
    KEY `idx_book_bookshelf_id` (`bookshelf_id`) USING BTREE,
    FULLTEXT KEY `ft_book_title_author` (`title`, `author`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍表';

-- ============================================================
-- 3. category — 类别表（自引用树形结构）
-- ============================================================
CREATE TABLE `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '类别ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '类别名称',
    `parent_id`   BIGINT      DEFAULT NULL COMMENT '父类别ID（自引用）',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_category_parent_id` (`parent_id`) USING BTREE,
    KEY `idx_category_sort_order` (`sort_order`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='类别表';

-- ============================================================
-- 4. borrow_record — 借阅记录表（状态机核心）
-- ============================================================
CREATE TABLE `borrow_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '借阅记录ID',
    `user_id`       BIGINT        NOT NULL COMMENT '用户ID',
    `book_id`       BIGINT        NOT NULL COMMENT '书籍ID',
    `borrow_date`   DATE          NOT NULL COMMENT '借阅日期',
    `due_date`      DATE          NOT NULL COMMENT '应还日期',
    `return_date`   DATE          DEFAULT NULL COMMENT '实际归还日期',
    `status`        VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED/BORROWED/RENEWED/RETURNED',
    `reject_reason` VARCHAR(500)  DEFAULT NULL COMMENT '拒绝原因',
    `renew_count`   INT           NOT NULL DEFAULT 0 COMMENT '续借次数',
    `fine_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '逾期罚款金额',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_borrow_user_id` (`user_id`) USING BTREE,
    KEY `idx_borrow_book_id` (`book_id`) USING BTREE,
    KEY `idx_borrow_user_status` (`user_id`, `status`) USING BTREE,
    KEY `idx_borrow_due_date` (`due_date`) USING BTREE,
    KEY `idx_borrow_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ============================================================
-- 5. bookshelf — 书架表
-- ============================================================
CREATE TABLE `bookshelf` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '书架ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '书架名称',
    `location`    VARCHAR(100) DEFAULT NULL COMMENT '书架位置',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '书架描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_bookshelf_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书架表';

-- ============================================================
-- 6. book_category_rel — 书籍-类别关联表（M:N 中间表）
-- ============================================================
CREATE TABLE `book_category_rel` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `book_id`     BIGINT NOT NULL COMMENT '书籍ID',
    `category_id` BIGINT NOT NULL COMMENT '类别ID',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_book_category` (`book_id`, `category_id`) USING BTREE,
    KEY `idx_bcr_category_id` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍-类别关联表';

-- ============================================================
-- 7. favorite — 用户收藏表
-- ============================================================
CREATE TABLE `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `book_id`    BIGINT   NOT NULL COMMENT '书籍ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_favorite_user_book` (`user_id`, `book_id`) USING BTREE,
    KEY `idx_favorite_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ============================================================
-- 8. notification — 通知表
-- ============================================================
CREATE TABLE `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`    BIGINT       NOT NULL COMMENT '接收用户ID',
    `type`       VARCHAR(30)  NOT NULL COMMENT '通知类型：SYSTEM/SUBSCRIPTION/OVERDUE',
    `title`      VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content`    TEXT         DEFAULT NULL COMMENT '通知内容',
    `is_read`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_notification_user_id` (`user_id`) USING BTREE,
    KEY `idx_notification_user_read` (`user_id`, `is_read`) USING BTREE,
    KEY `idx_notification_type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================================
-- 9. announcement — 公告表
-- ============================================================
CREATE TABLE `announcement` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`      TEXT         NOT NULL COMMENT '公告内容',
    `publisher_id` BIGINT       NOT NULL COMMENT '发布者ID（管理员）',
    `is_published` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否发布：0=草稿，1=已发布',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_announcement_publisher` (`publisher_id`) USING BTREE,
    KEY `idx_announcement_published` (`is_published`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================================
-- 10. operation_log — 操作日志表
-- ============================================================
CREATE TABLE `operation_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator`    VARCHAR(50)  NOT NULL COMMENT '操作人用户名',
    `action`      VARCHAR(100) NOT NULL COMMENT '操作动作',
    `target_type` VARCHAR(50)  NOT NULL COMMENT '操作目标类型（如 book/user/borrow）',
    `target_id`   BIGINT       DEFAULT NULL COMMENT '操作目标ID',
    `detail`      JSON         DEFAULT NULL COMMENT '操作详情（JSON 格式）',
    `ip`          VARCHAR(45)  DEFAULT NULL COMMENT '操作IP地址',
    `result`      VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILURE',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_operation_log_time` (`created_at`) USING BTREE,
    KEY `idx_operation_log_operator` (`operator`) USING BTREE,
    KEY `idx_operation_log_target` (`target_type`, `target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 11. subscription — 图书订阅表
-- ============================================================
CREATE TABLE `subscription` (
    `id`         BIGINT     NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
    `user_id`    BIGINT     NOT NULL COMMENT '用户ID',
    `book_id`    BIGINT     NOT NULL COMMENT '书籍ID',
    `is_active`  TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=取消，1=启用',
    `created_at` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    `updated_at` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_subscription_user_book` (`user_id`, `book_id`) USING BTREE,
    KEY `idx_subscription_book_id` (`book_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书订阅表';

-- ============================================================
-- 12. message — 读者留言/建议表
-- ============================================================
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '留言ID',
    `user_id`     BIGINT       NOT NULL COMMENT '留言用户ID',
    `content`     TEXT         NOT NULL COMMENT '留言内容',
    `reply`       TEXT         DEFAULT NULL COMMENT '管理员回复',
    `reply_at`    DATETIME     DEFAULT NULL COMMENT '回复时间',
    `replier_id`  BIGINT       DEFAULT NULL COMMENT '回复者ID（管理员）',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_message_user_id` (`user_id`) USING BTREE,
    KEY `idx_message_created_at` (`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='读者留言/建议表';


-- ============================================================
-- 种子数据
-- ============================================================

-- 默认管理员账号
-- 账号: admin    密码: a123456
-- BCrypt 密文通过 BCryptPasswordEncoder.encode("a123456") 生成
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `role`, `status`) VALUES
('admin', '$2b$10$zVbt8GcpDMc4T345twzJz.a5oHkrSBdj7UPaKOvwwtDMIfuc080ZK', 'admin@booknexus.com', NULL, 'ADMIN', 'ENABLED');

-- 默认图书类别种子数据（10 个根类别）
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('文学',     NULL, 1),
('科学',     NULL, 2),
('技术',     NULL, 3),
('教育',     NULL, 4),
('历史',     NULL, 5),
('哲学',     NULL, 6),
('艺术',     NULL, 7),
('经济',     NULL, 8),
('医学',     NULL, 9),
('其他',     NULL, 10);

-- 默认书架种子数据（A 区书架 1-10）
INSERT INTO `bookshelf` (`name`, `location`, `description`) VALUES
('A 区书架 1',  'A 区第 1 排', '文学类书籍'),
('A 区书架 2',  'A 区第 2 排', '科学类书籍'),
('A 区书架 3',  'A 区第 3 排', '技术类书籍'),
('A 区书架 4',  'A 区第 4 排', '教育类书籍'),
('A 区书架 5',  'A 区第 5 排', '历史类书籍'),
('A 区书架 6',  'A 区第 6 排', '哲学类书籍'),
('A 区书架 7',  'A 区第 7 排', '艺术类书籍'),
('A 区书架 8',  'A 区第 8 排', '经济类书籍'),
('A 区书架 9',  'A 区第 9 排', '医学类书籍'),
('A 区书架 10', 'A 区第 10 排', '综合类书籍');
