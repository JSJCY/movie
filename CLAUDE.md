# CLAUDE.md

## 项目概述

电影影评观影系统。前端 Vue 3 + Element Plus，后端 Spring Boot + Spring Cloud 微服务架构。

## 技术栈

- **后端**: Spring Boot 3.2.x + Spring Cloud 2023.0.x + Spring Cloud Alibaba 2023.0.1.0
- **注册/配置中心**: Nacos 2.3.x
- **API 网关**: Spring Cloud Gateway
- **ORM**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0（每个微服务独立数据库）
- **缓存**: Redis 7
- **消息队列**: RocketMQ 5.1.x
- **前端**: Vue 3.4 + Vite 5 + TypeScript 5 + Element Plus 2.7.x
- **基础设施**: Docker Compose

## 微服务架构

| 服务 | 端口 | 职责 |
|------|------|------|
| movie-gateway | 8080 | API 网关，路由转发，JWT 全局鉴权 |
| movie-user | 8081 | 用户注册/登录，JWT，个人资料 |
| movie-movie | 8082 | 电影 CRUD，分类/演员/导演，搜索 |
| movie-review | 8083 | 影评打分(1-10)，评论点赞 |
| movie-watchlist | 8084 | 想看/看过/在看，观影历史 |
| movie-ranking | 8085 | 周/月排行榜，定时任务 |

## 项目结构

```
D:\code\movie\
├── movie-parent/          # Maven 父工程（7 个模块）
│   ├── movie-common/      # 公共 DTO、异常、工具类
│   ├── movie-gateway/     # Spring Cloud Gateway
│   ├── movie-user/        # 用户服务
│   ├── movie-movie/       # 电影服务
│   ├── movie-review/      # 影评服务
│   ├── movie-watchlist/   # 观影记录服务
│   └── movie-ranking/     # 排行服务
├── movie-frontend/        # Vue 3 前端
├── docker/mysql/init/     # 数据库初始化 SQL
├── docker-compose.yml     # 基础设施编排
└── CLAUDE.md
```

## 开发约定

- 使用中文沟通
- 每个微服务独立数据库，不允许跨服务直连数据库
- 服务间通过 API 或消息队列通信
- Gateway 统一鉴权，解析后的用户信息通过 Header (X-User-Id, X-User-Role) 透传下游
- 统一响应格式: ApiResponse<T> { code, message, data }
