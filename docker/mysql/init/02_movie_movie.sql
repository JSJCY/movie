-- movie_movie 库 - 电影服务
SET NAMES utf8mb4;

CREATE TABLE `movie` (
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`          VARCHAR(200) NOT NULL COMMENT '中文片名',
    `original_title` VARCHAR(200) COMMENT '原始片名',
    `release_date`   DATE         COMMENT '上映日期',
    `duration`       INT          COMMENT '片长(分钟)',
    `country`        VARCHAR(100) COMMENT '制片国家/地区',
    `language`       VARCHAR(100) COMMENT '语言',
    `description`    TEXT         COMMENT '剧情简介',
    `poster_url`     VARCHAR(500) COMMENT '海报 URL',
    `trailer_url`    VARCHAR(500) COMMENT '预告片 URL',
    `average_rating` DECIMAL(3,1) DEFAULT 0.0 COMMENT '平均评分',
    `rating_count`   INT          DEFAULT 0    COMMENT '评分人数',
    `status`         TINYINT      DEFAULT 1    COMMENT '1上架 0下架',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_title` (`title`),
    INDEX `idx_release_date` (`release_date`),
    INDEX `idx_country` (`country`),
    INDEX `idx_average_rating` (`average_rating`),
    FULLTEXT INDEX `ft_title_desc` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影表';

CREATE TABLE `category` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名',
    `description` VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE `movie_category` (
    `movie_id`    BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    PRIMARY KEY (`movie_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影分类关联表';

CREATE TABLE `actor` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`         VARCHAR(100) NOT NULL COMMENT '姓名',
    `avatar_url`   VARCHAR(500),
    `bio`          TEXT,
    `birth_date`   DATE,
    `nationality`  VARCHAR(50),
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演员表';

CREATE TABLE `director` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`         VARCHAR(100) NOT NULL,
    `avatar_url`   VARCHAR(500),
    `bio`          TEXT,
    `birth_date`   DATE,
    `nationality`  VARCHAR(50),
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导演表';

CREATE TABLE `movie_actor` (
    `movie_id`       BIGINT NOT NULL,
    `actor_id`       BIGINT NOT NULL,
    `character_name` VARCHAR(100) COMMENT '饰演角色名',
    PRIMARY KEY (`movie_id`, `actor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影演员关联表';

CREATE TABLE `movie_director` (
    `movie_id`   BIGINT NOT NULL,
    `director_id` BIGINT NOT NULL,
    PRIMARY KEY (`movie_id`, `director_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影导演关联表';

-- 预置分类
INSERT INTO `category` (`name`, `description`) VALUES
('动作', '包含打斗、追逐等动作元素的电影'),
('喜剧', '以幽默搞笑为主要风格的电影'),
('剧情', '以故事情节为主的电影'),
('科幻', '涉及科学幻想元素的电影'),
('恐怖', '以惊悚恐怖为主题的电影'),
('爱情', '以爱情故事为主线的情影'),
('动画', '以动画形式制作的电影'),
('纪录片', '纪录真实事件或人物的电影'),
('悬疑', '以悬念推理为看点的电影'),
('犯罪', '围绕犯罪故事展开的电影');
