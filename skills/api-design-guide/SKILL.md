---
name: api-design-guide
description: RESTful API 设计规范，涵盖 URL 设计、HTTP 方法与状态码、统一响应格式、认证授权、接口文档、版本管理及常见设计模式。适用于 Spring Boot / Node.js / Go 等后端技术栈。
license: MIT
---

# RESTful API 设计规范

## 概述

本规范定义 RESTful API 设计的通用标准，包括 URL 命名、HTTP 方法选择、响应格式、认证方案、文档策略及常见设计模式。适用于所有后端技术栈的 Web API 项目。

---

## 1. RESTful URL 设计规范

### 1.1 核心原则

| 原则 | 说明 |
|------|------|
| **资源名词，而非动词** | URL 应描述资源，不包含操作动词 |
| **使用复数名词** | `/users` 而非 `/user` |
| **全小写 + 连字符** | `/borrow-records` 而非 `/borrowRecords` 或 `/BORROW_RECORDS` |
| **层级表示关系** | `/users/{id}/orders` 表示用户的订单 |
| **限制嵌套层级 ≤ 2** | 超过 2 级用查询参数替代 |

### 1.2 URL 设计示例

```
✅ 正确
GET    /api/v1/books                  # 获取书籍列表
GET    /api/v1/books/{id}             # 获取单本书
POST   /api/v1/books                  # 创建书籍
PUT    /api/v1/books/{id}             # 全量更新书籍
PATCH  /api/v1/books/{id}             # 部分更新书籍
DELETE /api/v1/books/{id}             # 删除书籍
GET    /api/v1/users/{id}/orders      # 获取用户订单 (2级嵌套)

❌ 错误
GET    /api/v1/getBooks               # 含动词
GET    /api/v1/getBookById            # 含动词 + 参数
POST   /api/v1/createBook             # 含动词
GET    /api/v1/users/{id}/orders/{oid}/items  # 3级嵌套
```

### 1.3 资源命名参考

| 业务域 | 资源名 | URL 前缀 |
|--------|--------|----------|
| 用户 | users | `/api/v1/users` |
| 订单 | orders | `/api/v1/orders` |
| 商品 | products | `/api/v1/products` |
| 文件 | files | `/api/v1/files` |
| 配置 | configs | `/api/v1/configs` |
| 统计 | statistics | `/api/v1/statistics` |
| 认证 | auth | `/api/v1/auth` |

### 1.4 特殊操作命名

对于无法映射到标准 CRUD 的操作，使用资源子动作：

```
POST   /api/v1/orders/{id}/cancel     # 取消订单
POST   /api/v1/orders/{id}/confirm    # 确认订单
POST   /api/v1/users/{id}/reset-password  # 重置密码
POST   /api/v1/auth/login             # 登录
POST   /api/v1/auth/logout            # 登出
POST   /api/v1/auth/refresh           # 刷新令牌
```

---

## 2. HTTP 方法与状态码

### 2.1 HTTP 方法使用规范

| 方法 | 语义 | 幂等性 | 安全性 | 典型场景 |
|------|------|--------|--------|----------|
| GET | 查询资源 | ✅ 是 | ✅ 是 | 列表查询、详情查询 |
| POST | 创建资源 | ❌ 否 | ❌ 否 | 新增、登录、复杂查询 |
| PUT | 全量替换 | ✅ 是 | ❌ 否 | 全量更新（覆盖所有字段） |
| PATCH | 部分更新 | ❌ 否 | ❌ 否 | 局部字段更新 |
| DELETE | 删除资源 | ✅ 是 | ❌ 否 | 物理/逻辑删除 |
| HEAD | 获取头信息 | ✅ 是 | ✅ 是 | 检查资源是否存在 |
| OPTIONS | 获取支持的请求方法 | ✅ 是 | ✅ 是 | CORS 预检请求 |

> **幂等性**：多次相同请求对服务端产生的效果一致（GET 永不修改数据；PUT 每次覆盖为相同值；DELETE 删除后再次删除仍为已删除状态）
>
> **安全性**：不修改服务端任何数据

### 2.2 HTTP 状态码使用规范

#### 成功类 (2xx)

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| **200 OK** | 请求成功 | GET、PUT、PATCH 成功，或业务错误场景（项目决定） |
| **201 Created** | 资源已创建 | POST 创建成功，建议在 Location 头返回新资源 URL |
| **202 Accepted** | 请求已接受（异步处理） | 异步任务、批量处理等长时间操作 |
| **204 No Content** | 成功但无返回体 | DELETE 成功、PUT/PATCH 无需返回数据时 |

#### 客户端错误 (4xx)

| 状态码 | 含义 | 典型触发场景 |
|--------|------|-------------|
| **400 Bad Request** | 请求参数错误 | 参数校验失败、JSON 格式错误 |
| **401 Unauthorized** | 未认证 | Token 缺失、无效或过期 |
| **403 Forbidden** | 无权限 | 已登录但角色/权限不足 |
| **404 Not Found** | 资源不存在 | ID 对应记录不存在 |
| **405 Method Not Allowed** | 方法不允许 | GET 访问只支持 POST 的接口 |
| **409 Conflict** | 资源冲突 | 唯一键重复、状态流转不合法 |
| **422 Unprocessable Entity** | 验证失败 | 业务规则校验不通过 |
| **429 Too Many Requests** | 请求过多 | 触发限流 |

#### 服务端错误 (5xx)

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| **500 Internal Server Error** | 服务器内部错误 | 未预期异常、空指针等 |
| **502 Bad Gateway** | 网关错误 | 上游服务不可达 |
| **503 Service Unavailable** | 服务不可用 | 维护中、熔断等 |
| **504 Gateway Timeout** | 网关超时 | 上游服务响应超时 |

### 2.3 状态码使用策略

推荐两种策略，项目应统一选择其一：

**策略 A：HTTP 状态码 + 业务码分离**
- HTTP 层返回实际状态码（200/201/400/401/404/500）
- 响应体中的 `code` 为业务错误码
- 适用于：需要与 API 网关、CDN、监控系统深度集成的项目

**策略 B：HTTP 200 + 业务码统一**
- 所有业务响应 HTTP 层统一返回 200
- 只有协议层错误（认证失败、服务器异常）才用 4xx/5xx
- 适用于：前端统一处理、简化拦截器逻辑的项目

---

## 3. 统一响应格式

### 3.1 标准响应结构

所有 API 响应使用统一外壳：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1705312200000
}
```

字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | Integer | 状态码，与 HTTP 状态码一致或使用自定义业务码 |
| `message` | String | 提示信息，成功时为 "success" 或 "操作成功" |
| `data` | Object/Array/null | 业务数据，错误时通常为 null |
| `timestamp` | Long | 服务器时间戳（毫秒），便于排查问题 |

### 3.2 成功响应示例

**单条数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "email": "zhangsan@example.com",
    "createdAt": "2025-01-15T08:30:00"
  },
  "timestamp": 1705312200000
}
```

**列表数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "name": "张三" },
    { "id": 2, "name": "李四" }
  ],
  "timestamp": 1705312200000
}
```

**创建成功（返回 201）：**
```json
{
  "code": 201,
  "message": "创建成功",
  "data": { "id": 123 },
  "timestamp": 1705312200000
}
```

### 3.3 分页响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      { "id": 1, "name": "张三" },
      { "id": 2, "name": "李四" }
    ],
    "total": 150,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 8,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1705312200000
}
```

分页字段说明：

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | Array | 当前页数据 |
| `total` | Long | 总记录数 |
| `pageNum` | Integer | 当前页码（1-based） |
| `pageSize` | Integer | 每页条数 |
| `pages` | Integer | 总页数 |
| `hasNext` | Boolean | 是否有下一页 |
| `hasPrev` | Boolean | 是否有上一页 |

### 3.4 错误响应示例

**参数校验错误（422）：**
```json
{
  "code": 422,
  "message": "参数校验失败",
  "data": {
    "errors": [
      { "field": "email", "message": "邮箱格式不正确" },
      { "field": "age", "message": "年龄不能小于 0" }
    ]
  },
  "timestamp": 1705312200000
}
```

**业务错误（409）：**
```json
{
  "code": 409,
  "message": "该邮箱已被注册",
  "data": null,
  "timestamp": 1705312200000
}
```

**认证失败（401）：**
```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null,
  "timestamp": 1705312200000
}
```

### 3.5 响应头规范

| Header | 示例 | 说明 |
|--------|------|------|
| `Content-Type` | `application/json; charset=utf-8` | 响应格式 |
| `X-Request-Id` | `a1b2c3d4-...` | 请求追踪 ID |
| `X-RateLimit-Remaining` | `99` | 剩余请求次数 |
| `X-RateLimit-Reset` | `1705312800` | 限流重置时间戳 |
| `X-Total-Count` | `150` | 总记录数（配合分页，供简单场景使用） |
| `Location` | `/api/v1/books/123` | 新创建资源的 URL（配合 201） |

---

## 4. 请求参数规范

### 4.1 路径参数 (Path Parameters)

用于标识具体资源：

```
GET /api/v1/users/{userId}/orders/{orderId}
```

规则：
- 用 `{name}` 包裹参数名（API 文档中），实际 URL 中直接使用值
- 参数名使用小驼峰：`userId`、`orderId`
- 路径参数应为必需参数，不允许为空

### 4.2 查询参数 (Query Parameters)

用于过滤、排序、搜索：

```
GET /api/v1/books?keyword=spring&categoryId=3&status=AVAILABLE&pageNum=1&pageSize=20
```

命名规则：
- 小驼峰命名：`pageNum`、`createdAt`
- 布尔值使用 `true`/`false` 字符串
- 多选值用逗号分隔：`status=AVAILABLE,BORROWED`
- 范围值用 From/To 后缀：`priceFrom=10&priceTo=50`

### 4.3 请求体 (Request Body)

POST/PUT/PATCH 使用 JSON 请求体：

```json
{
  "name": "张三",
  "email": "zhangsan@example.com",
  "age": 25,
  "tags": ["vip", "new"],
  "address": {
    "province": "广东省",
    "city": "深圳市"
  }
}
```

规则：
- `Content-Type: application/json`
- 字段名使用小驼峰：`createdAt`
- 日期时间使用 ISO 8601 格式：`"2025-01-15T08:30:00"`
- 日期使用：`"2025-01-15"`
- 空值处理：可选字段不传或传 `null`

### 4.4 分页/排序参数标准

| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `pageNum` | int | 1 | 页码，从 1 开始 |
| `pageSize` | int | 20 | 每页条数，最大 100 |
| `orderBy` | string | `id` | 排序字段 |
| `orderDir` | string | `desc` | 排序方向：asc / desc |

### 4.5 搜索参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| `keyword` | string | 全局搜索关键词（模糊匹配标题、内容等） |
| `startDate` / `endDate` | string | 日期范围过滤 |
| `minXxx` / `maxXxx` | type | 数值范围过滤，如 `minPrice`、`maxPrice` |

### 4.6 参数校验

所有入参必须做校验：

```json
// 请求体校验
{
  "name": "张三",                         // @NotBlank
  "email": "zhangsan@example.com",        // @Email
  "age": 25,                              // @Min(0) @Max(150)
  "role": "ADMIN"                          // @Pattern(regexp = "ADMIN|USER")
}
```

校验失败返回 422，`data.errors` 中列出所有错误字段和原因。

---

## 5. 认证与授权

### 5.1 常见方案对比

| 方案 | 原理 | 优点 | 缺点 | 适用场景 |
|------|------|------|------|----------|
| **Session + Cookie** | 服务器存储会话，Cookie 传递 Session ID | 状态由服务端控制，可随时失效 | 不适用于分布式；不支持跨域；有 CSRF 风险 | 传统 SSR 应用、内部系统 |
| **JWT (JSON Web Token)** | 客户端持有签名令牌，无状态验证 | 分布式友好；跨域支持；无需服务端存储 | Token 无法主动失效（需配合黑名单）；payload 过大影响带宽 | 微服务、前后端分离、移动 App |
| **OAuth 2.0** | 第三方授权协议，颁发 Access Token | 标准化；支持多方授权；Token 可刷新 | 实现复杂度高；需要授权服务器 | 开放平台、第三方登录 |
| **API Key** | 预先分配的密钥，通过 Header 传递 | 实现简单；无状态 | 无法区分用户；泄露风险高 | 内部服务调用、简单第三方 API |

### 5.2 JWT 方案详解 (推荐)

**Token 结构：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzA1MzEyMjAwLCJleHAiOjE3MDUzOTg2MDB9.xxx
```

**Token 类型：**

| 类型 | 有效期 | 存储位置 | 说明 |
|------|--------|----------|------|
| Access Token | 15分钟 ~ 2小时 | 内存（避免 localStorage） | 访问业务接口 |
| Refresh Token | 7天 ~ 30天 | HttpOnly Cookie | 刷新 Access Token |

**Token Payload 最小化原则：**
```json
{
  "sub": "1001",         // 用户 ID
  "roles": ["ADMIN"],    // 角色列表
  "iat": 1705312200,     // 签发时间
  "exp": 1705398600      // 过期时间
}
```

> 不要在 JWT payload 中存放密码、完整用户信息等敏感数据。

**刷新流程：**
```
1. 客户端用 Access Token 请求业务接口
2. 返回 401 → 客户端用 Refresh Token 请求 /auth/refresh
3. 获得新的 Access Token，继续请求
4. Refresh Token 也过期 → 跳转登录页
```

### 5.3 权限控制模式

**RBAC (Role-Based Access Control) —— 推荐：**
```
用户 → 角色 (ADMIN / LIBRARIAN / MEMBER) → 权限
```

**接口权限声明示例：**
```
GET    /api/v1/users              [ADMIN]
GET    /api/v1/users/me           [认证用户即可]
POST   /api/v1/books              [ADMIN, LIBRARIAN]
GET    /api/v1/books              [公开]
```

**API Key 方案（内部服务间调用）：**
```
X-API-Key: sk-xxxxxxxxxxxxxxxxxxxx
```

- 使用 UUID 生成高熵密钥
- 支持绑定 IP 白名单
- 记录每次调用的审计日志

---

## 6. 接口文档自动化

### 6.1 三种方案对比

| 方案 | 生态 | 优点 | 缺点 | 推荐场景 |
|------|------|------|------|----------|
| **SpringDoc OpenAPI** (1.6+) | Spring Boot | 兼容 Swagger 3；注解驱动；自动生成；支持 OpenAPI 3.0 | 注解较多可能影响代码可读性 | Spring Boot 项目 |
| **SpringFox Swagger 2** | Spring Boot | 成熟稳定；社区资源丰富 | 已停止维护；仅支持 OpenAPI 2.0 | 老项目维护 |
| **Knife4j** | Spring Boot | 国产；UI 美观；在线调试功能强；支持离线文档导出 | 依赖 SpringDoc/Swagger；需额外配置 | 国内团队、需要离线文档 |

### 6.2 SpringDoc OpenAPI 接入 (Spring Boot)

**Maven 依赖：**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**基本配置：**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("项目 API 文档")
                .version("1.0")
                .description("项目接口文档")
                .contact(new Contact().name("团队").email("team@example.com")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

**Controller 注解：**
```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "获取用户列表", description = "分页查询所有用户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未认证")
    })
    @GetMapping
    public Result<PageResult<UserResponse>> list(UserQueryRequest request) { ... }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) { ... }
}
```

**实体注解：**
```java
@Schema(description = "用户创建请求")
public class UserCreateRequest {
    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email
    private String email;
}
```

**访问地址：**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 6.3 Knife4j 接入 (Spring Boot)

**Maven 依赖：**
```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.4.0</version>
</dependency>
```

**配置（application.yml）：**
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: 'default'
      paths-to-match: '/**'
      packages-to-scan: com.example.controller

knife4j:
  enable: true
  setting:
    language: zh_cn
    enable-footer: false
```

**访问地址：**
- Knife4j UI: `http://localhost:8080/doc.html`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### 6.4 文档规范要求

- [ ] 每个 Controller 添加 `@Tag` 注解
- [ ] 每个接口添加 `@Operation` 注解（含 summary 和 description）
- [ ] 每个接口添加 `@ApiResponses` 列出所有可能的响应码
- [ ] 每个请求 DTO 的字段添加 `@Schema` 注解（含 example）
- [ ] 请求参数校验注解齐全（`@NotBlank`、`@Email`、`@Min` 等）
- [ ] 生产环境可关闭 Swagger UI（通过配置开关）

---

## 7. API 版本管理

### 7.1 三种版本策略对比

| 策略 | 方式 | 优点 | 缺点 | 推荐度 |
|------|------|------|------|--------|
| **URL 路径** | `/api/v1/books` `/api/v2/books` | 直观；易于调试；浏览器友好 | URL 不"干净"；目录级路由变更 | ⭐⭐⭐⭐⭐ |
| **请求头** | `Accept: application/vnd.api.v2+json` | URL 不变；适合内容协商 | 不直观；调试不便；缓存策略复杂 | ⭐⭐⭐ |
| **查询参数** | `/api/books?version=2` | URL 不变；后端易实现 | 污染查询参数；污染缓存 | ⭐⭐ |

### 7.2 URL 路径版本（推荐）

```
/api/v1/books          # v1 版本
/api/v2/books          # v2 主版本（可能有破坏性变更）
/api/v1.1/books        # v1.1 次版本（向后兼容的增量更新）
```

**版本号规则：**
- `v1` → `v2`：破坏性变更（字段改名、删除、类型变更、业务逻辑大幅修改）
- `v1` → `v1.1`：非破坏性增量（新增可选字段、新增接口、默认值变更）

### 7.3 版本共存策略

```
package com.example.controller.v1;
→ BookControllerV1.java

package com.example.controller.v2;
→ BookControllerV2.java
```

- 新老版本共存至少 1~2 个大版本周期
- 旧版接口标记 `@Deprecated` 并在响应头添加 `Deprecation: true` 和 `Sunset: <日期>`
- 通过监控旧版调用量，逐步下线

### 7.4 何时需要升级版本

| 变更类型 | 是否需要升级版本 |
|----------|------------------|
| 新增接口 | ❌ 不需要 |
| 新增可选请求/响应字段 | ❌ 不需要 |
| 删除请求/响应字段 | ✅ 需要（主版本） |
| 字段重命名 | ✅ 需要（主版本） |
| 字段类型变更（如 String → Integer） | ✅ 需要（主版本） |
| 业务逻辑大幅变更 | ✅ 需要（主版本） |
| 新增必填请求字段 | ✅ 需要（主版本） |
| 调整分页默认值 | ⚠️ 建议次版本 |

---

## 8. 常见 API 设计模式

### 8.1 分页

```
GET /api/v1/users?pageNum=1&pageSize=20&orderBy=id&orderDir=desc
```

**分页参数约束：**
- `pageNum` 最小为 1
- `pageSize` 默认为 20，最大为 100（服务端强校验）
- 超过最大值的 pageSize 自动截断为最大值
- 超过总页数的 pageNum 返回空列表

### 8.2 搜索

**简单搜索（单字段模糊匹配）：**
```
GET /api/v1/books?keyword=spring
```

**高级搜索（多条件精确过滤）：**
```
GET /api/v1/books?title=Spring&author=Craig&categoryId=3&status=AVAILABLE
```

**全文搜索（ElasticSearch / MySQL 全文索引）：**
```
POST /api/v1/books/search
{
  "keyword": "spring boot",
  "fields": ["title", "description"],
  "filters": {
    "categoryId": 3,
    "status": "AVAILABLE"
  }
}
```

> 搜索建议：简单场景用查询参数；复杂条件（10+ 字段）使用 POST + JSON 请求体。

### 8.3 批量操作

**批量创建：**
```
POST /api/v1/users/batch
["POST /api/v1/users/batch"]
[
  { "name": "张三", "email": "zhangsan@example.com" },
  { "name": "李四", "email": "lisi@example.com" }
]
```

响应返回部分成功/失败明细：
```json
{
  "code": 200,
  "message": "部分成功",
  "data": {
    "successCount": 8,
    "failureCount": 2,
    "results": [
      { "index": 0, "id": 201, "success": true },
      { "index": 1, "id": null, "success": false, "message": "邮箱已存在" }
    ]
  }
}
```

**批量删除：**
```
DELETE /api/v1/users/batch?ids=1,2,3,4,5
```

**批量更新状态：**
```
PATCH /api/v1/orders/batch/status
{
  "ids": [1, 2, 3],
  "status": "CANCELLED"
}
```

**批量操作约束：**
- 单次批量操作最多 100 条（防内存溢出和长事务）
- 批量操作使用事务包裹，但允许部分成功/失败（非严格一致性场景）
- 批量操作返回明细，方便前端展示处理结果

### 8.4 文件上传

**单文件上传：**
```
POST /api/v1/files/upload
Content-Type: multipart/form-data

file: document.pdf
type: DOCUMENT
```

**多文件上传：**
```
POST /api/v1/files/batch-upload
Content-Type: multipart/form-data

files: [image1.png, image2.jpg, image3.png]
type: IMAGE
```

**文件上传响应：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileName": "a1b2c3d4-document.pdf",
    "originalName": "document.pdf",
    "url": "https://cdn.example.com/files/a1b2c3d4-document.pdf",
    "size": 2048576,
    "mimeType": "application/pdf"
  }
}
```

**文件上传约束：**
- 单文件最大 10MB（业务按需调整）
- 文件名校验：防止路径穿越（`../` 等）
- 文件类型白名单校验（MIME + 扩展名双重校验）
- 文件存储使用 UUID 重命名，避免原始文件名冲突
- 大文件走分片上传 + 秒传机制

### 8.5 导出

```
POST /api/v1/books/export
{
  "filters": { "categoryId": 3 },
  "format": "EXCEL",        // EXCEL / CSV / PDF
  "columns": ["title", "author", "isbn", "price"]
}
```

**同步导出**（数据量 < 5000 条）：
- 直接返回文件流，`Content-Type: application/octet-stream`

**异步导出**（数据量较大）：
- 返回任务 ID，客户端轮询下载
- 配合 202 Accepted 状态码

```json
{
  "code": 202,
  "message": "导出任务已提交",
  "data": { "taskId": "task-abc-123" }
}
```

### 8.6 导入

```
POST /api/v1/books/import
Content-Type: multipart/form-data

file: books_import.xlsx
```

**导入响应：**
```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 100,
    "successCount": 95,
    "failureCount": 5,
    "errors": [
      { "row": 23, "message": "ISBN 已存在" },
      { "row": 58, "message": "作者名称为空" }
    ]
  }
}
```

---

## 9. 附加规范

### 9.1 日期时间格式

| 场景 | 格式 | 示例 |
|------|------|------|
| 时间戳 | ISO 8601 | `"2025-01-15T08:30:00Z"` |
| 日期 | ISO 8601 | `"2025-01-15"` |
| 传输时区 | 统一使用 UTC | 前端负责本地化显示 |

### 9.2 空值处理

- 响应中：使用 `@JsonInclude(NON_NULL)` 省略 null 字段
- 数组字段：返回 `[]` 而非 null
- 字符串字段：返回 `""` 或省略（根据上下文）

### 9.3 安全规范

- 所有接口强制 HTTPS（生产环境）
- 敏感数据不在 URL 查询参数中传递
- 请求体大小限制（如 10MB）
- 敏感接口实施速率限制（登录：每分钟 5 次）
- 输出前统一编码，防止 XSS
- SQL 使用参数化查询，防止注入

### 9.4 命名一致性对照

| 层 | 命名风格 | 示例 |
|----|----------|------|
| URL / 查询参数 / JSON 字段 | camelCase | `userId`, `createdAt` |
| 数据库表名 | snake_case | `user_profile` |
| 数据库字段名 | snake_case | `created_at` |
| Java 类名（后端场景） | PascalCase | `UserController` |
| Java 属性名（后端场景） | camelCase | `private String userName;` |

---

## 10. 开发 Checklist

新接口开发或旧接口变更时，请确认以下事项：

- [ ] URL 遵循 RESTful 规范（名词复数、无动词、限制嵌套）
- [ ] HTTP 方法选择正确（GET/POST/PUT/PATCH/DELETE）
- [ ] API 版本号已包含在 URL 中
- [ ] 请求参数有完整的校验注解
- [ ] 响应使用统一 Result 外壳格式
- [ ] 列表接口实现了分页
- [ ] 列表接口支持排序和基本筛选
- [ ] 所有可能的错误场景有对应的状态码和错误消息
- [ ] Controller 已添加 OpenAPI/Knife4j 文档注解
- [ ] 认证要求已明确标注（公开 / 认证 / 特定角色）
- [ ] 敏感接口已配置速率限制
- [ ] 破坏性变更已创建新版本而非直接修改
- [ ] 文件上传接口有类型和大小校验

---

## 参考资料

- [RESTful API 设计最佳实践](https://restfulapi.net/)
- [OpenAPI 3.0 规范](https://spec.openapis.org/oas/latest.html)
- [JWT 官方文档](https://jwt.io/introduction)
- [HTTP 状态码 (MDN)](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status)
- [JSON API 规范](https://jsonapi.org/)
