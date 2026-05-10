-- movie_watchlist 库 - 观影记录服务
CREATE TABLE `watch_record` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT      NOT NULL,
    `movie_id`   BIGINT      NOT NULL,
    `status`     VARCHAR(20) NOT NULL COMMENT 'WANT_TO_WATCH / WATCHING / WATCHED',
    `rating`     TINYINT     COMMENT '用户打分',
    `watched_at` DATETIME    COMMENT '看完时间',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_movie` (`user_id`, `movie_id`),
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_movie_id` (`movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观影记录表';
