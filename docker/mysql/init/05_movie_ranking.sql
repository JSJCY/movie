-- movie_ranking 库 - 排行服务
CREATE TABLE `ranking_snapshot` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `period_type`  VARCHAR(20) NOT NULL COMMENT 'WEEKLY / MONTHLY',
    `period_start` DATE        NOT NULL COMMENT '统计周期开始',
    `period_end`   DATE        NOT NULL COMMENT '统计周期结束',
    `movie_id`     BIGINT      NOT NULL,
    `rank`         INT         NOT NULL COMMENT '排名',
    `avg_rating`   DECIMAL(3,1) COMMENT '周期平均分',
    `review_count` INT         DEFAULT 0 COMMENT '周期评论数',
    `watch_count`  INT         DEFAULT 0 COMMENT '周期观看数',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_period_rank` (`period_type`, `period_end`, `rank`),
    INDEX `idx_movie_period` (`movie_id`, `period_type`, `period_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行快照表';
