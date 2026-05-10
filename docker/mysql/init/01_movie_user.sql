-- movie_user 库 - 用户服务
CREATE TABLE `user` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL       COMMENT 'BCrypt 密码哈希',
    `email`         VARCHAR(100) UNIQUE         COMMENT '邮箱',
    `phone`         VARCHAR(20)  UNIQUE         COMMENT '手机号',
    `nickname`      VARCHAR(50)                 COMMENT '昵称',
    `avatar_url`    VARCHAR(500)                COMMENT '头像 URL',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `status`        TINYINT      NOT NULL DEFAULT 1   COMMENT '1正常 0禁用',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_email` (`email`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

