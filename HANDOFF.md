# 电影影评观影系统 — 前端交接文档

## 1. 快速启动

### 基础设施（必须先启动）
```bash
cd D:\code\movie
docker compose up -d
# 等待所有容器就绪：docker ps 确认 10 个容器全部 Up
```

| 容器 | 端口 | 说明 |
|------|------|------|
| movie-nacos | 8848/9848 | 注册中心，控制台 http://localhost:8848/nacos (nacos/nacos) |
| movie-mysql-user / movie / review / watchlist / ranking | 3307-3311 | MySQL 8.0 各服务独立库 (root/root123) |
| movie-redis | 6379 | Redis (密码 redis123) |
| movie-rmq-namesrv / broker | 9876/10911 | RocketMQ |

### 后端（6 个微服务）
```bash
cd D:\code\movie\movie-parent

# 1. 先编译公共模块
mvn -pl movie-common install -DskipTests

# 2. 依次启动（各开一个终端，顺序建议先 gateway）
mvn -pl movie-gateway spring-boot:run     # 8080
mvn -pl movie-user spring-boot:run        # 8081
mvn -pl movie-movie spring-boot:run       # 8082
mvn -pl movie-review spring-boot:run      # 8083
mvn -pl movie-watchlist spring-boot:run   # 8084
mvn -pl movie-ranking spring-boot:run     # 8085
```

### 前端
```bash
cd D:\code\movie\movie-frontend
npm install
npm run dev
# → http://localhost:3000
# Vite 自动代理 /api → localhost:8080 (gateway)
```

### 默认账号
- 管理员：`admin / admin123`
- 注册入口：`http://localhost:3000/register`

---

## 2. 架构概览

```
浏览器 (localhost:3000)
    │  Vite Proxy /api → localhost:8080
    ▼
movie-gateway (8080)  ← JWT 全局鉴权 + 路由转发
    │  Header 透传: X-User-Id, X-User-Role
    ├── movie-user (8081)      用户注册/登录/JWT/个人资料
    ├── movie-movie (8082)     电影CRUD/分类/演员/导演/搜索
    ├── movie-review (8083)    影评打分(1-10)/点赞/统计
    ├── movie-watchlist (8084)  想看/看过/在看/观影历史
    └── movie-ranking (8085)   周/月排行榜/Bayesian加权
```

**关键规则**：
- 每个微服务独立数据库，不允许跨服务直连
- 服务间通过 RestTemplate + Nacos 服务发现互相调用
- Gateway 统一鉴权，JWT 白名单路径无需 Token
- 统一响应格式：`{ code: 200, message: "success", data: T }`
- 前端 Axios 拦截器已处理 JWT 附加与 401 跳转登录

---

## 3. 后端 API 完整清单

### 3.1 认证 — `/api/auth`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 无 | 登录 { username, password } → LoginVO |
| POST | `/api/auth/register` | 无 | 注册 { username, password, email, nickname? } → LoginVO |
| POST | `/api/auth/refresh` | 无 | 刷新 Token ?refreshToken=xxx → LoginVO |

**LoginVO 响应**：
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": { "id":1, "username":"admin", "email":"...", "nickname":"...", "role":"ADMIN", "avatarUrl":null }
}
```

### 3.2 用户 — `/api/users` + `/api/admin/users`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/users/me` | JWT | 获取当前用户信息 |
| PUT | `/api/users/me` | JWT | 更新个人资料 { nickname?, email?, phone? } |
| PUT | `/api/users/me/avatar` | JWT | 更新头像 ?avatarUrl=xxx |
| GET | `/api/admin/users` | JWT+ADMIN | 用户列表 ?page=&size= |
| PUT | `/api/admin/users/{id}/status` | JWT+ADMIN | 禁用/启用 { status:0|1 } |
| PUT | `/api/admin/users/{id}/role` | JWT+ADMIN | 修改角色 { role:"USER"|"ADMIN" } |

### 3.3 电影 — `/api/movies`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/movies` | 无 | 电影列表 ?page=&size=&categoryId=&country=&keyword=&sortBy=(created_at|rating|release_date) |
| GET | `/api/movies/{id}` | 无 | 电影详情 → MovieVO (含演员/导演/分类) |
| GET | `/api/movies/search?q=` | 无 | 全文搜索 |
| GET | `/api/movies/batch?ids=1,2,3` | 无 | **新增** 批量查询轻量信息 → MovieBriefVO[] |
| POST | `/api/movies` | JWT | 创建电影 |
| PUT | `/api/movies/{id}` | JWT | 更新电影 |
| DELETE | `/api/movies/{id}` | JWT | 下架电影 (软删除) |
| POST | `/api/movies/import/tmdb/{tmdbId}` | JWT | **新增** 从 TMDB 导入电影 ?tmdbId=550 (需配置 API Key) |

### 3.4 分类 — `/api/categories`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/categories` | 无 | 分类列表 |
| POST | `/api/categories` | JWT | 创建分类 { name, description } |
| DELETE | `/api/categories/{id}` | JWT | 删除分类 |

预置 10 个分类：动作、喜剧、剧情、科幻、恐怖、爱情、动画、纪录片、悬疑、犯罪

### 3.5 演员 — `/api/actors`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/actors` | 无 | 演员列表 ?page=&size=&keyword= |
| POST | `/api/actors` | JWT | 创建演员 |
| PUT | `/api/actors/{id}` | JWT | 更新演员 |
| DELETE | `/api/actors/{id}` | JWT | 删除演员 |

### 3.6 导演 — `/api/directors`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/directors` | 无 | 导演列表 ?page=&size=&keyword= |
| POST | `/api/directors` | JWT | 创建导演 |
| PUT | `/api/directors/{id}` | JWT | 更新导演 |
| DELETE | `/api/directors/{id}` | JWT | 删除导演 |

### 3.7 影评 — `/api/reviews`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/reviews/movie/{movieId}` | 可选 | 电影影评列表 ?page=&size= |
| GET | `/api/reviews/user/{userId}` | 可选 | 用户影评列表 |
| GET | `/api/reviews/movie/{movieId}/stats` | 无 | 评分统计 → ReviewStatsVO。可选 ?startDate=2026-01-01&endDate=2026-01-31 |
| POST | `/api/reviews` | JWT | 发影评 { movieId, rating:1-10, content? } |
| PUT | `/api/reviews/{id}` | JWT | 修改影评 |
| DELETE | `/api/reviews/{id}` | JWT | 删除 (管理员可删他人) |
| POST | `/api/reviews/{id}/like` | JWT | 点赞 |
| DELETE | `/api/reviews/{id}/like` | JWT | 取消点赞 |

### 3.8 观影记录 — `/api/watchlist`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/watchlist/me` | JWT | 我的观影 ?status=&page=&size= |
| GET | `/api/watchlist/me/history` | JWT | 观看历史 ?page=&size= |
| GET | `/api/watchlist/movie/{movieId}/my-status` | JWT | 我对某电影的状态 |
| GET | `/api/watchlist/movie/{movieId}/watch-count` | 无 | **新增** 观影人数统计 ?startDate=&endDate= (内部调用) |
| POST | `/api/watchlist` | JWT | 添加记录 { movieId, status, rating? } |
| PUT | `/api/watchlist/{id}` | JWT | 更新记录 |
| DELETE | `/api/watchlist/{id}` | JWT | 删除记录 |

### 3.9 排行榜 — `/api/rankings`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/rankings/weekly` | 无 | 周排行 ?period=2026-W19 |
| GET | `/api/rankings/monthly` | 无 | 月排行 ?period=2026-05 |

**RankingVO 响应**：
```json
{ "rank":1, "movieId":1, "movieTitle":"xxx", "moviePoster":"http://...", "avgRating":8.5, "reviewCount":120, "watchCount":500 }
```
排行榜由定时任务自动计算：周榜周日 23:00，月榜月末 23:00。空库时返回空数组。

---

## 4. 前端现状

### 已完成的页面

| 路由 | 页面 | 状态 |
|------|------|------|
| `/` | HomeView | 首页，基础布局 |
| `/movies` | MovieList | 电影列表 + 分类筛选 + 排序 + 分页 |
| `/movies/:id` | MovieDetail | 详情 + 影评 + 观影状态管理（功能齐全） |
| `/search` | MovieSearch | 搜索页 |
| `/rankings` | RankingView | 周/月排行切换，显示 rank + 海报 + 评分 + 评论数 + 观看数 |
| `/login` | LoginView | 登录 |
| `/register` | RegisterView | 注册 |
| `/profile` | UserProfile | 个人资料编辑 |
| `/my/watchlist` | WatchlistView | 我的观影（想看/在看/看过筛选 + 状态变更） |
| `/admin/movies` | MovieManage | 后台电影管理列表 |
| `/admin/movies/create` `/admin/movies/:id/edit` | MovieForm | 电影新增/编辑表单 |
| `/admin/users` | UserManage | 用户管理（状态/角色修改） |

### 前端文件结构

```
movie-frontend/src/
├── api/
│   ├── index.ts          Axios 实例，baseURL=/api，JWT 拦截器，401 自动跳登录
│   ├── auth.ts           认证 API (login/register/refresh/users/admin)
│   ├── movie.ts          电影+分类+演员+导演 API
│   ├── ranking.ts        排行榜 API
│   ├── review.ts         影评 API
│   └── watchlist.ts      观影记录 API
├── router/index.ts       路由 + 导航守卫 (requiresAuth/requiresAdmin/guestOnly)
├── stores/auth.ts        Pinia 认证状态
├── types/index.ts        TS 类型定义 (所有 VO + 请求体)
├── views/
│   ├── HomeView.vue
│   ├── NotFound.vue
│   ├── auth/             LoginView, RegisterView
│   ├── movie/            MovieList, MovieDetail, MovieSearch
│   ├── ranking/          RankingView
│   ├── watchlist/        WatchlistView
│   ├── user/             UserProfile
│   └── admin/            AdminLayout, MovieManage, MovieForm, UserManage
└── utils/format.ts       日期/状态格式化工具
```

### 前端技术栈
- Vue 3.4 + TypeScript 5 + Vite 5
- Element Plus 2.7.x
- Pinia (状态管理) + Vue Router 4
- Axios (HTTP)

---

## 5. 前端待办事项

### 高优先级

1. ~~**RankingView 数据展示验证**~~ — ✅ 已导入 15 部 TMDB 热门电影 + 影评，排行榜返回 17 条数据，包含标题和海报。
2. ~~**WatchlistView 卡片优化**~~ — ✅ 已修复。后端 `WatchlistServiceImpl` 现在通过 movie-service 批量接口自动填充 `movieTitle`/`moviePoster`。
3. **MovieForm 分类/演员/导演选择** — 创建/编辑电影时需要选择分类、演员、导演，当前表单依赖 `categories.value` 从 API 加载，演员/导演列表需要确认是否完善。

### 中优先级

4. **电影列表无海报占位** — `MovieList.vue` 使用 `placehold.co` 占位图，可替换为项目内置占位图。
5. **排行榜无手动刷新入口** — 排行数据依赖定时任务，可能为空的周期显示 "暂无排行数据"。可以考虑加一个"计算"按钮，但需要后端新增触发接口。
6. **搜索页完善** — `MovieSearch.vue` 存在但需确认与搜索 API 的对接是否完整。
7. **用户头像上传** — 目前 `updateAvatar` API 只接受 URL，无文件上传功能。

### 低优先级

8. **响应式布局优化** — `MovieList.vue` 使用 CSS Grid 4 列，小屏幕可能需要 2-3 列。
9. **分页组件统一** — 各页面分页参数不完全一致（有 12、10、20），可统一配置。
10. **Loading/Empty/Error 状态覆盖** — 各页面的加载态和空数据态已基本处理，错误态可进一步增强。

---

## 6. 已知问题与修复记录

### 已修复
- ✅ **分类中文乱码** — 根因：MySQL Docker 容器初始化时 `character_set_client=latin1`，SQL 文件中中文被双重 UTF-8 编码。修复：`02_movie_movie.sql` 顶部加 `SET NAMES utf8mb4;` + 手动重插数据。
- ✅ **排行榜 500 错误** — 根因：`rank` 字段是 MySQL 保留关键字。修复：`RankingSnapshot.java` 添加 `@TableField("\`rank\`")`。
- ✅ **Gateway 鉴权绕过** — `/api/watchlist/movie/{id}/my-status` 被 GET 白名单放行，改为需要 JWT。
- ✅ **ReviewServiceImpl NPE** — `getMovieStats` 中 avg 为 null 时使用 BigDecimal.ZERO 兜底。

### 注意规避

- **Docker Desktop 内存** — 10 个容器约需 5GB，建议 `%USERPROFILE%\.wslconfig` 限制 WSL2 为 2-3GB。
- **8080 端口占用** — 启动 gateway 前 `netstat -ano | findstr ":8080"` 检查。
- **TMDB API Key** — 已配置在 `movie-movie/src/main/resources/application.yml`。当前 Key 可用，导入端点 `POST /api/movies/import/tmdb/{tmdbId}`。如 Key 失效，去 https://www.themoviedb.org/settings/api 申请免费 Key 替换。
- **Nacos 鉴权开启** — `docker-compose.yml` 中 `NACOS_AUTH_ENABLE=true`，控制台登录 `nacos/nacos`。
- **curl 传中文 JSON** — Windows Git Bash 下 curl POST 中文数据会 UTF-8 编码错误，建议用 Postman 或前端页面测试。

---

## 7. 数据库速查

| 服务 | 库名 | 端口 | 密码 |
|------|------|------|------|
| movie-user | movie_user | 3307 | root123 |
| movie-movie | movie_movie | 3308 | root123 |
| movie-review | movie_review | 3309 | root123 |
| movie-watchlist | movie_watchlist | 3310 | root123 |
| movie-ranking | movie_ranking | 3311 | root123 |

种子数据在 `D:\code\movie\docker\mysql\init\` 目录下，每个库一个 SQL 文件。

---

## 8. 关键联系人 / 参考

- Git 仓库：`https://github.com/JSJCY/movie`（私有），当前分支 `master`
- Nacos 控制台：`http://localhost:8848/nacos`（nacos/nacos），可查看所有注册服务
- 后端统一响应格式 `ApiResponse<T>`：`src/main/java/com/movie/common/dto/ApiResponse.java`
- 前端 Axios 封装：`src/api/index.ts`，已自动解包 `data`，所以业务代码中 `res.data` = 后端 `ApiResponse.data`
