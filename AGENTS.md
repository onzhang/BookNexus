# BookNexus 图书管理系统 — AGENTS.md

## 项目概览

| 项目 | 内容 |
|------|------|
| 名称 | BookNexus 图书管理系统 |
| 开发者 | 张俊文（个人完成） |
| 架构 | 生产级单体后端（Spring Boot 3.3 + JDK 21） |
| 状态 | **M2 已完成** — 数据库设计 + 前端脚手架就绪，**可进入 M3 核心功能开发** |
| 工期 | 预计 30 个工作日，共 6 个里程碑 |
| 最新计划 | `docs/项目计划书.md`（v2.0 最终版，含脑暴决议） |
When you need to search docs, use `context7` tools.

## 进度状态

| 里程碑 | 天数 | 状态 |
|--------|------|------|
| ✅ **M1** 项目骨架 + 中间件连通 | 第 1-3 天 | **已完成** |
| ✅ **M2** 数据库设计 + 前端脚手架 | 第 4-8 天 | **已完成** |
| ❌ **M3** 核心功能开发（用户/书籍/借阅 + 前端 + 测试） | 第 9-20 天 | 未开始 |
| ❌ **M4** 增强功能开发（含测试） | 第 21-25 天 | 未开始 |
| ❌ **M5** 定时任务 + ES 搜索 | 第 26-28 天 | 未开始 |
| ❌ **M6** 全量回归测试 + 性能优化 + 部署 | 第 29-30 天 | 未开始 |

**关键变更**（相对于原 7 里程碑计划）：
- 合并为 6 个里程碑（M2+M3 原计划合并为 M2，原 M7 并入 M6）
- 测试嵌入每个功能开发阶段（非延迟到 M6）
- 新增前端开发纳入里程碑（全栈项目）
- 里程碑总天数仍为 30 天

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.3.5 |
| 语言 | JDK | 21 |
| ORM | MyBatis-Plus + mybatis-plus-join | 3.5.9 |
| 数据库 | MySQL + Druid | 8.0+ / 1.2.23 |
| 缓存 | Redis (Lettuce) + Redisson + Caffeine | 7.x / 3.37.0 / 3.1.8 |
| 消息 | RabbitMQ (AMQP) + 死信队列 | 3.x |
| 搜索 | Elasticsearch (Spring Data ES) | 8.x |
| 调度 | XXL-Job | 2.4.1 |
| 限流 | Sentinel | 1.8.8 |
| 存储 | MinIO | 8.5.14 |
| 认证 | JJWT | 0.12.6 |
| 监控 | Spring Boot Actuator | 3.3.5 |
| 工具 | Hutool / EasyExcel / MapStruct / Knife4j | — |

## 项目结构

```
BookNexus/
├── pom.xml                         # Maven 构建 (Java 21, Spring Boot 3.3.5)
├── .gitignore
├── AGENTS.md                       # ← 本文件
├── docker-compose-vm.yml           # VM 端中间件 Docker Compose (RabbitMQ + ES + MinIO)
├── docs/
│   ├── 项目计划书.md               # 完整项目计划（v2.0 最终版）
│   ├── skills介绍文档.md           # AI Skills 安装指南
│   └── ER-diagram.md               # E-R 图 (Mermaid 格式, 12 张表)
├── skills/                         # AI agent skills（9 个技能目录）
│   ├── api-design-guide/
│   ├── database-design-guide/
│   ├── frontend-design/
│   ├── java-backend-workflow/
│   ├── java-spring-boot-guide/
│   ├── springboot-project-guide/
│   ├── vue3-frontend-guide/
│   ├── skill-creator/
│   └── git-workflow-guide/
├── sql/
│   ├── init.sql                    # DDL（12 张表 + 索引）+ 种子数据
│   └── es_book_mapping.json        # ES 书籍索引映射 (ik + pinyin)
├── frontend/                       # Vue 3 + Vite + TypeScript 前端
│   ├── package.json
│   ├── vite.config.ts              # 端口 90, 代理 /api → localhost:8080
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── api/index.ts            # Axios 实例 (JWT 拦截器)
│       ├── router/index.ts         # Vue Router (token 守卫)
│       ├── stores/user.ts          # Pinia 用户状态
│       ├── stores/app.ts           # Pinia 应用状态
│       ├── types/index.ts
│       └── views/
├── src/main/java/com/zjw/booknexus/
│   ├── BookNexusApplication.java
│   ├── common/
│   │   ├── BaseEntity.java         # 审计字段基类 (Serializable)
│   │   ├── Result.java             # 统一 API 响应（requestId 流体式注入）
│   │   └── PageResult.java         # 统一分页响应
│   ├── config/
│   │   ├── ESConfig.java           # ES 客户端配置
│   │   ├── MinIOConfig.java        # MinIO 客户端 + bucket 自动创建
│   │   ├── MyBatisPlusConfig.java  # 分页插件 + 乐观锁插件
│   │   ├── MyMetaObjectHandler.java # MyBatis-Plus 审计字段自动填充
│   │   ├── RabbitMQConfig.java     # 3 交换机 + 5 队列 + 死信队列
│   │   ├── RedisConfig.java        # RedisTemplate + CacheManager
│   │   ├── SentinelConfig.java     # @SentinelResource 注解支持
│   │   ├── WebMvcConfig.java       # CORS + 登录拦截器 + Jackson 转换器
│   │   └── XXLJobConfig.java       # XXL-Job 执行器（空地址自动跳过）
│   ├── entity/                     # 数据库实体 (12 个)
│   │   ├── User.java               # extends BaseEntity
│   │   ├── Book.java               # extends BaseEntity
│   │   ├── Category.java           # extends BaseEntity
│   │   ├── BorrowRecord.java       # extends BaseEntity (含 BigDecimal fineAmount)
│   │   ├── Bookshelf.java          # extends BaseEntity
│   │   ├── BookCategoryRel.java    # M:N 中间表 (implements Serializable)
│   │   ├── Favorite.java           # 仅 id+created_at (implements Serializable)
│   │   ├── Notification.java       # extends BaseEntity
│   │   ├── Announcement.java       # extends BaseEntity
│   │   ├── OperationLog.java       # 仅 id+created_at (implements Serializable)
│   │   ├── Subscription.java       # id+created_at+updated_at (implements Serializable)
│   │   └── Message.java            # extends BaseEntity
│   ├── mapper/                     # MyBatis-Plus Mapper (12 个)
│   │   ├── UserMapper.java
│   │   ├── BookMapper.java
│   │   ├── CategoryMapper.java
│   │   ├── BorrowRecordMapper.java
│   │   ├── BookshelfMapper.java
│   │   ├── BookCategoryRelMapper.java
│   │   ├── FavoriteMapper.java
│   │   ├── NotificationMapper.java
│   │   ├── AnnouncementMapper.java
│   │   ├── OperationLogMapper.java
│   │   ├── SubscriptionMapper.java
│   │   └── MessageMapper.java
│   ├── enums/                      # 枚举类 (5 个)
│   │   ├── UserRole.java           # ADMIN, USER
│   │   ├── UserStatus.java         # ENABLED, DISABLED
│   │   ├── BorrowStatus.java       # PENDING → RETURNED 状态机
│   │   ├── BookStatus.java         # AVAILABLE, BORROWED, DAMAGED, LOST
│   │   └── NotificationType.java   # SYSTEM, SUBSCRIPTION, OVERDUE
│   ├── exception/
│   │   ├── BusinessException.java  # 业务异常（携带错误码）
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器（统一 ResponseEntity）
│   └── interceptor/
│       └── LoginInterceptor.java   # JWT 拦截器骨架（M3 实现）
└── src/main/resources/
    ├── application.yml             # 主配置（Knife4j 扫描根包）
    ├── application-dev.yml         # 开发环境（MySQL/Redis 本地, RabbitMQ/ES/MinIO → VM）
    ├── application-test.yml        # 测试环境（独立数据库实例）
    ├── application-prod.yml        # 生产环境（环境变量注入）
    └── logback-spring.xml          # 控制台 + 文件日志（30天轮转 + MDC）
```

**注意**: `controller/`, `service/`, `dto/`, `vo/`, `utils/` 目录尚未创建，需在 M3 阶段搭建。

## 架构分层

```
接入层 (Controller)        — Sentinel 限流 → JWT 拦截 → 参数校验
业务层 (Service)           — Redis 缓存 → MQ 异步 → XXL-Job 定时
数据访问层 (Mapper/DAO)    — MyBatis-Plus → Druid → MySQL
中间件                     — Redis / RabbitMQ / Elasticsearch / MinIO
```

## 数据库设计（计划 14 张表）

| 表 | 优先级 |
|----|--------|
| `user`, `book`, `category`, `borrow_record` | P0 |
| `bookshelf`, `book_category_rel` | P1 |
| `favorite`, `notification`, `announcement`, `operation_log` | P2 |
| `subscription`, `message` | P3 |
| `xxl_job_*` | P4 |

每张表含审计字段：`id`, `created_at`, `updated_at`, `is_deleted`。

## RESTful API 设计

- 前缀: `/api/v1/admin/**` (管理员)、`/api/v1/user/**` (用户)、`/api/v1/public/**` (公开)
- 统一响应: `{ code, message, data, requestId, timestamp }`
- 分页响应: 含 `records`, `total`, `page`, `size`, `pages`
- 状态码: 200/201/400/401/403/404/409/429/500
- 错误码: 字符串枚举（如 `BOOK_NOT_FOUND`, `BORROW_LIMIT_EXCEEDED`）
- 异常响应: 统一 `ResponseEntity<Result<Void>>`，动态映射 HTTP 状态码

## RabbitMQ 事件设计

| 交换机 | 路由键 | 队列 | 用途 |
|--------|--------|------|------|
| `book.exchange` | `book.borrow` | `book.borrow.queue` | 借阅异步处理 |
| `book.exchange` | `book.return` | `book.return.queue` | 归还事件通知 |
| `book.exchange` | `book.subscribe` | `book.subscribe.queue` | 订阅通知推送 |
| `log.exchange` | `log.operation` | `log.operation.queue` | 操作日志写入 |
| `notice.exchange` | `notice.overdue` | `notice.overdue.queue` | 逾期催还通知 |
| `dlx.exchange` | `#` | `dlx.queue` | 死信队列（兜底） |

> 所有业务队列均配置死信交换机 `dlx.exchange`，重试耗尽后消息自动转入死信队列。

## 开发规范

### 命名
- Controller: `BookController`
- Service: `BookService` / `BookServiceImpl`
- Mapper: `BookMapper`
- Entity: `Book`（下划线转驼峰）
- DTO: `BookCreateReq` / `BookPageResp`
- VO: `BookVO`
- Config: `RedisConfig`

### 分层职责
- **Controller**: 接收请求、参数校验、调用 Service、返回 Result
- **Service**: 业务逻辑、事务管理、调用 Mapper/中间件
- **Mapper**: 数据库操作（BaseMapper 继承）、XML 自定义 SQL
- **Entity**: 数据库实体（继承 BaseEntity）
- **DTO**: 请求/响应数据传输对象
- **VO**: 视图对象（聚合展示数据）

### 异常处理
- 业务异常: `throw new BusinessException(code, message)`
- 全局捕获: `GlobalExceptionHandler` 统一转换为 `Result`
- 参数校验: `@Valid` + `@NotBlank` 等，无效返回 400

## 可用 Skills

项目 `skills/` 目录中包含了以下可直接引用的技能：

| 技能 | 用途 |
|------|------|
| [api-design-guide](skills/api-design-guide/SKILL.md) | RESTful API 设计规范 |
| [database-design-guide](skills/database-design-guide/SKILL.md) | MySQL 数据库设计规范 |
| [frontend-design](skills/frontend-design/SKILL.md) | 前端设计哲学 |
| [java-backend-workflow](skills/java-backend-workflow/SKILL.md) | Java 后端开发工作流 |
| [java-spring-boot-guide](skills/java-spring-boot-guide/SKILL.md) | Spring Boot + MyBatis 开发指南 |
| [springboot-project-guide](skills/springboot-project-guide/SKILL.md) | Spring Boot 项目结构规范 |
| [vue3-frontend-guide](skills/vue3-frontend-guide/SKILL.md) | Vue 3 + TypeScript 规范 |
| [skill-creator](skills/skill-creator/SKILL.md) | 创建/测试/优化 AI Skills |

另推荐安装的社区技能见 `docs/skills介绍文档.md`。

## Maven 构建

```bash
mvn clean compile          # 编译
mvn clean test -Pdev       # 测试（需中间件运行）
mvn clean package -Pprod   # 打包
mvn spring-boot:run        # 运行 (dev profile)
```

## 重要提醒

1. **LoginInterceptor.java** 中的 JWT 校验逻辑尚未实现（TODO），当前所有请求直接放行
2. **XXL-Job** 配置了空地址保护，本地开发时自动跳过执行器注册
3. **Sentinel** 当前仅注册了注解切面，具体限流规则需在业务代码中通过 `@SentinelResource` 定义
4. **Caffeine** 依赖已引入，但本地缓存集成代码尚未编写（需配合 Redis 做两级缓存）
5. `sql/init.sql` 中的管理员密码 BCrypt 密文为占位值，M3 认证模块开发时需通过 `BCryptPasswordEncoder` 重新生成
6. 中间件部署：MySQL + Redis 本地安装；RabbitMQ + ES + MinIO 部署于 VM (192.168.100.128)，见 `docker-compose-vm.yml`
7. 生产配置 `application-prod.yml` 中所有敏感信息通过环境变量注入，禁止硬编码
8. **Controller/Service/DTO/VO 代码由开发者在 M3 阶段编写**
9. **前端项目 `frontend/` 脚手架已创建**（Vite + Vue 3 + TS + Element Plus），页面组件待 M3 开发
10. **所有 test 代码均需开发者自行编写，AI 仅辅助生成骨架提示**

## M2 交付物清单

**完成日期**: 2026-04-30 | **产出**: 40 个新增/修改文件

| 类别 | 文件 | 说明 |
|------|------|------|
| 配置 | `docker-compose-vm.yml` | VM 端 RabbitMQ + ES + MinIO |
| 配置 | `application-dev.yml` | 3 个中间件 host → 192.168.100.128 |
| 配置 | `config/MyMetaObjectHandler.java` | MyBatis-Plus 审计字段自动填充 |
| SQL | `sql/init.sql` | DDL（12 张表）+ 索引 + 种子数据 |
| SQL | `sql/es_book_mapping.json` | ES 书籍索引映射（ik + pinyin） |
| 文档 | `docs/ER-diagram.md` | E-R 图（Mermaid 格式） |
| 实体 | `entity/*.java` | 12 个实体类（8 个继承 BaseEntity，4 个实现 Serializable） |
| Mapper | `mapper/*.java` | 12 个 Mapper 接口（继承 BaseMapper） |
| 枚举 | `enums/*.java` | 5 个枚举（UserRole/UserStatus/BorrowStatus/BookStatus/NotificationType） |
| 前端 | `frontend/` | Vite 5 + Vue 3.4 + TS + Element Plus + Pinia + Vue Router + Axios（端口 90） |

**验证状态**: `mvn compile` ✅ | `vue-tsc --noEmit` ✅ | `vite build` ✅

**代码审查**: 0 ERROR, 2 WARNING（已修复）

## M1 已修复问题清单

M1 审查发现的 18 个问题已全部修复。详细见 `docs/项目计划书.md`，要点如下：

| 类别 | 修复内容 |
|------|----------|
| 依赖 | 新增 actuator + mybatis-plus-join；artifactId 改为 kebab-case |
| 配置 | 新增 logback-spring.xml（30天轮转+MDC）+ application-test.yml |
| 配置类 | 乐观锁插件 / MinIO 桶自动创建 / RabbitMQ 死信队列 / XXL-Job 条件注册 / Jackson 转换器 |
| 公共组件 | BaseEntity 实现 Serializable / Result 流体式 requestId / 异常统一 ResponseEntity |
| 注释 | 全局异常处理器 Javadoc / logback/test.yml 企业级注释补全 |

## 脑暴会议关键决议

完整决议文档已合并入 `docs/项目计划书.md`，要点摘要：

- **角色**: 角色枚举判断（admin/user），非完整 RBAC
- **借阅**: 5本/30天/逾期0.1元/天，续借1次+15天
- **认证**: Access Token 30min + Refresh Token 7天（Redis 存储）
- **错误码**: 字符串枚举（如 `BOOK_NOT_FOUND`, `BORROW_LIMIT_EXCEEDED`）
- **日志**: 管理端所有写操作 + 用户端借阅/续借/取消订阅
- **搜索**: M3 先用 MySQL FULLTEXT，M5 迁移 ES
- **统计**: ECharts 仪表盘（总藏书/用户/借出/逾期 + 趋势排行）
- **请求追踪**: UUID 方案（HandlerInterceptor + MDC）
- **测试**: 每个功能开发完成后立即编写单元+集成测试，M6 全量回归
- **部署**: 简化部署，功能完整优先

## AI 交互约定

- **语言**: 默认使用**中文**回复，除非用户明确要求使用英文

## Git 版本控制规范

### GitHub 仓库初始化

1. **创建仓库**: 在 GitHub 网页上创建新仓库（不要勾选 "Initialize with README"）
2. **本地关联**: 获取仓库地址后执行以下命令
   ```bash
   git remote add origin <仓库地址>
   git branch -M master
   git push -u origin master
   ```
3. **同步其他分支**: 功能分支同步最新 master
   ```bash
   git checkout feature/m3-core-features
   git merge master
   git push origin feature/m3-core-features
   ```

### 分支策略

| 分支 | 用途 | 命名规范 |
|------|------|----------|
| master | 生产代码 | master |
| feature/* | 功能开发 | feature/m3-core-features |
| hotfix/* | 紧急修复 | hotfix/xxx |

### 基本操作命令

```bash
# 查看状态
git status

# 查看提交历史
git log --oneline -5

# 添加文件（不要用 git add .）
git add <具体文件路径>

# 提交
git commit -m "提交信息"

# 推送
git push

# 拉取并合并
git pull origin master

# 创建并切换分支
git checkout -b feature/xxx

# 合并分支（先切换到目标分支）
git checkout master
git merge feature/xxx
```

### 注意事项

1. **不要用 `git add .`** — 容易误提交 node_modules、target 等不需要的文件，先查看 `git status`
2. **提交信息规范** — 简明扼要，说明本次做了什么
3. **不要轻易 `git push -f`** — 会覆盖远程历史，造成协作问题
4. **不要轻易 `git reset --hard`** — 会丢失未提交的更改，操作前确认
5. **删除文件后要 commit** — `git restore` 可以恢复已删文件，但删除操作本身需要 commit
6. **合并分支前先 pull** — 确保本地有最新的远程代码
7. **敏感信息不上传** — .env、credentials.json 等不要 commit，已经上传的要用 git filter-branch 或 BFG 清理
8. **工作区干净再操作分支** — 切换分支前确保没有未提交的更改，或用 `git stash` 暂存

### 常见问题处理

| 场景 | 解决方案 |
|------|----------|
| 误删文件未 commit | `git restore <文件>` 恢复 |
| 误删文件已 commit | `git checkout HEAD~1 -- <文件>` 从上一个提交恢复 |
| 提交信息写错 | `git commit --amend` 修改（仅限本地未 push） |
| 想放弃本地修改 | `git restore .` 丢弃所有修改 |
| 暂存当前工作 | `git stash` 暂存，`git stash pop` 恢复 |

