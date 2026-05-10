-- movie_review 库 - 影评服务
CREATE TABLE `review` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id`   BIGINT       NOT NULL COMMENT '电影 ID',
    `user_id`    BIGINT       NOT NULL COMMENT '用户 ID',
    `rating`     TINYINT      NOT NULL COMMENT '评分 1-10',
    `content`    TEXT         COMMENT '影评内容',
    `like_count` INT          DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_movie_user` (`movie_id`, `user_id`),
    INDEX `idx_movie_id` (`movie_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `chk_rating` CHECK (`rating` BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影评表';

CREATE TABLE `review_like` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `review_id`  BIGINT   NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_review_user` (`review_id`, `user_id`),
    INDEX `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影评点赞表';
