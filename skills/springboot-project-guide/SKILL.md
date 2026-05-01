---
name: springboot-project-guide
description: |
  Spring Boot 项目结构、分层架构、命名规范、配置管理、常用依赖选型及开发最佳实践指南。
  适用于新建 Spring Boot 项目时的架构参考和代码规范约束，帮助团队保持一致的编码风格和项目组织方式。
  技术栈开放：支持 MyBatis/MyBatis-Plus/JPA、MySQL/PostgreSQL/H2、单体/微服务等多种组合。
license: MIT
---

# Spring Boot 项目结构与开发规范

> 本 Skill 提供 Spring Boot 项目的标准目录结构、分层架构设计、命名规范、配置管理和依赖选型指南。适用于单体应用和微服务项目的初始搭建及日常开发。

---

## 一、标准项目目录结构

```text
project-name/
├── pom.xml                                  # Maven 构建文件（或 build.gradle）
├── src/
│   ├── main/
│   │   ├── java/com/example/project/
│   │   │   ├── ProjectApplication.java      # Spring Boot 启动类（放在根包下）
│   │   │   ├── config/                      # 配置类
│   │   │   │   ├── WebConfig.java           # Web MVC 配置（CORS、拦截器、消息转换器）
│   │   │   │   ├── SecurityConfig.java      # 安全配置（Spring Security / Shiro）
│   │   │   │   ├── DataSourceConfig.java    # 数据源配置
│   │   │   │   ├── RedisConfig.java         # 缓存配置
│   │   │   │   └── SwaggerConfig.java       # API 文档配置
│   │   │   ├── controller/                  # 控制层 - REST API 入口
│   │   │   │   ├── UserController.java
│   │   │   │   └── OrderController.java
│   │   │   ├── service/                     # 业务层 - 接口定义
│   │   │   │   ├── UserService.java
│   │   │   │   └── OrderService.java
│   │   │   ├── service/impl/                # 业务层 - 接口实现
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   └── OrderServiceImpl.java
│   │   │   ├── mapper/                      # 数据访问层（MyBatis）或 repository/（JPA）
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── OrderMapper.java
│   │   │   ├── entity/                      # 数据库实体（DO - Data Object）
│   │   │   │   ├── User.java
│   │   │   │   └── Order.java
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   │   ├── request/                 # 入参 DTO（接收前端请求）
│   │   │   │   │   ├── UserCreateRequest.java
│   │   │   │   │   └── UserQueryRequest.java
│   │   │   │   └── response/                # 出参 DTO（返回前端数据）
│   │   │   │       ├── UserResponse.java
│   │   │   │       └── PageResult.java
│   │   │   ├── vo/                          # 视图对象（可选，组合多个实体字段）
│   │   │   │   └── UserProfileVO.java
│   │   │   ├── enums/                       # 枚举类
│   │   │   │   ├── UserStatus.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── constant/                    # 常量定义
│   │   │   │   └── SystemConstant.java
│   │   │   ├── exception/                   # 自定义异常
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── interceptor/                 # 拦截器（认证、日志、限流）
│   │   │   │   └── AuthInterceptor.java
│   │   │   ├── aspect/                      # AOP 切面（日志、权限、缓存）
│   │   │   │   └── LogAspect.java
│   │   │   ├── converter/                   # 对象转换器（Entity <-> DTO <-> VO）
│   │   │   │   └── UserConverter.java
│   │   │   └── utils/                       # 工具类
│   │   │       ├── JwtUtils.java
│   │   │       └── DateUtils.java
│   │   └── resources/
│   │       ├── application.yml              # 主配置文件
│   │       ├── application-dev.yml          # 开发环境配置
│   │       ├── application-test.yml         # 测试环境配置
│   │       ├── application-prod.yml         # 生产环境配置
│   │       ├── application-staging.yml      # 预发布/灰度环境配置（可选）
│   │       ├── mapper/                      # MyBatis XML 映射文件（与 mapper/ 对应）
│   │       │   ├── UserMapper.xml
│   │       │   └── OrderMapper.xml
│   │       ├── db/migration/                # 数据库迁移脚本（Flyway / Liquibase）
│   │       │   └── V1__init_schema.sql
│   │       └── logback-spring.xml           # 日志配置
│   └── test/                                # 测试代码
│       └── java/com/example/project/
│           ├── controller/                  # Controller 层测试
│           ├── service/                     # Service 层单元测试
│           └── mapper/                      # Mapper 层集成测试
├── sql/                                     # 数据库初始化脚本（与 Flyway 二选一）
│   └── schema.sql
├── docs/                                    # 项目文档
├── docker/                                  # Docker 相关配置
│   ├── Dockerfile
│   └── docker-compose.yml
├── .gitignore
└── README.md
```

### 包结构要点

| 原则 | 说明 |
|------|------|
| 启动类放根包 | `@SpringBootApplication` 所在类必须位于根包（如 `com.example.project`），确保组件扫描覆盖所有子包 |
| 功能分包 | 以业务功能为主进行分包（如 `controller/user/`），而不是按技术层分包（如 `controllers/`、`services/`） |
| 接口与实现分离 | Service 层采用 `接口` + `impl/实现类` 模式，便于单元测试 Mock 和扩展 |
| DTO/VO/Entity 分离 | 不同用途的对象放在不同包中，禁止混用（Entity 不能直接返回给前端） |

---

## 二、分层架构详解

### 2.1 分层职责矩阵

| 层级 | 职责 | 允许操作 | 禁止操作 |
|------|------|----------|----------|
| **Controller** | 接收 HTTP 请求、参数校验、调用 Service、封装响应 | 参数校验 (`@Valid`)、请求参数绑定、调用 Service、返回 DTO/Result | 编写业务逻辑、直接调用 Mapper、操作事务 |
| **Service** | 业务逻辑编排、事务管理、调用 Mapper/外部服务 | 业务判断、数据转换、调用多个 Mapper、`@Transactional` | 处理 HTTP 对象（`HttpServletRequest`/`HttpServletResponse`） |
| **Mapper/Repository** | 数据库 CRUD 操作、SQL 映射 | SQL 执行、数据持久化、简单查询 | 编写业务逻辑、调用其他 Mapper/Service、跨表业务判断 |
| **Entity/DO** | 数据库表映射、纯数据载体 | 字段与表列映射、Lombok 注解 | 包含业务方法（除简单 getter/setter 外） |
| **DTO** | 层间数据传输、前端交互契约 | 数据暂存、校验注解 (`@NotNull` 等)、API 文档注解 | 包含业务逻辑、数据库映射注解 |

### 2.2 典型调用链路

```text
Client (Browser/App)
  │
  ▼
Controller           ← 接收请求，参数校验 (@Valid)
  │
  ▼
Service              ← 业务逻辑，事务管理 (@Transactional)
  │
  ▼
Mapper/Repository    ← 数据库操作 (SQL/JPQL)
  │
  ▼
Database             ← 持久化存储
```

### 2.3 代码示例

**Controller 层：**

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取用户")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return Result.success(user);
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.success(userId);
    }
}
```

**Service 层：**

```java
public interface UserService {
    UserResponse getUserById(Long id);
    Long createUser(UserCreateRequest request);
}

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return userConverter.toResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        // 业务校验
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        User user = userConverter.toEntity(request);
        userMapper.insert(user);
        log.info("用户创建成功，ID: {}", user.getId());
        return user.getId();
    }
}
```

**Mapper 层：**

```java
@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    int insert(User user);

    boolean existsByUsername(@Param("username") String username);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.project.mapper.UserMapper">
    
    <select id="selectById" resultType="User">
        SELECT * FROM user WHERE id = #{id} AND deleted = 0
    </select>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user (username, email, password, status, created_at, updated_at)
        VALUES (#{username}, #{email}, #{password}, #{status}, NOW(), NOW())
    </insert>
</mapper>
```

---

## 三、命名规范

### 3.1 代码命名

| 类型 | 规范 | 示例 | 说明 |
|------|------|------|------|
| 项目名 | 大驼峰 (PascalCase) | `BookNexus`、`OrderService` | Maven artifactId 使用 kebab-case: `book-nexus` |
| 包名 | 全小写，点分隔 | `com.example.project` | 对应域名倒序，禁止使用下划线 |
| 类名 | 大驼峰 (PascalCase) | `UserServiceImpl`、`OrderController` | 接口不加 `I` 前缀 |
| 接口名 | 大驼峰 (PascalCase) | `UserService`、`UserMapper` | 不使用 `IUserService` |
| 实现类名 | 接口名 + `Impl` | `UserServiceImpl` | 放在 `service/impl/` 子包下 |
| 方法名 | 小驼峰 (camelCase) | `getUserById`、`createOrder` | 动词开头，语义清晰 |
| 变量名 | 小驼峰 (camelCase) | `userName`、`orderList` | 布尔类型用 `is`/`has`/`can` 前缀 |
| 常量名 | 全大写 + 下划线 | `MAX_PAGE_SIZE`、`DEFAULT_TIMEOUT` | 放在 `constant/` 包中 |
| 枚举值 | 全大写 + 下划线 | `ACTIVE`、`ORDER_PAID` | 枚举类名用大驼峰：`UserStatus` |

### 3.2 方法命名约定（推荐）

| 操作 | 前缀 | 示例 | 说明 |
|------|------|------|------|
| 新增 | `insert` / `save` / `create` | `insertUser`、`createOrder` | `insert` 侧重单表，`create` 侧重业务 |
| 删除 | `delete` / `remove` | `deleteUserById`、`removeItem` | 软删除用 `delete`，物理删除用 `remove` |
| 修改 | `update` | `updateUserEmail`、`updateOrderStatus` | 明确更新什么字段 |
| 查询单个 | `get` / `find` | `getUserById`、`findByUsername` | `get` 期望结果存在，`find` 允许不存在 |
| 查询列表 | `list` / `query` | `listUsers`、`queryOrders` | `list` 简单列表，`query` 含复杂条件 |
| 分页查询 | `page` / `search` | `pageUsers`、`searchBooks` | 返回分页结果 |
| 统计 | `count` | `countActiveUsers` | 返回数字 |
| 判断 | `is` / `has` / `exists` | `existsByUsername` | 返回 boolean |

### 3.3 数据库命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 数据库名 | 小写 + 下划线 | `book_nexus`、`order_system` |
| 表名 | 小写 + 下划线，单数名词 | `user`、`book`、`borrow_record` |
| 关联表名 | 两个表名拼接 | `user_role`、`book_category` |
| 主键 | `id`，BIGINT 自增 或 UUID | `id` |
| 外键 | 关联表名 + `_id` | `user_id`、`book_id` |
| 普通字段 | 小写 + 下划线 | `user_name`、`email`、`created_at` |
| 审计字段 | 统一命名 | `created_at`、`updated_at`、`created_by`、`updated_by` |
| 逻辑删除 | `deleted` / `is_deleted` | TINYINT，0-未删除，1-已删除 |
| 索引名 | `idx_表名_字段名` | `idx_user_email`、`idx_order_user_id` |
| 唯一索引 | `uk_表名_字段名` | `uk_user_username` |

---

## 四、配置管理

### 4.1 多环境配置策略

```yaml
# application.yml - 主配置（公共配置，使用 spring.profiles.active 切换环境）
server:
  port: 8080

spring:
  application:
    name: project-name
  profiles:
    active: dev  # 默认使用开发环境
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null  # null 值不返回
```

```yaml
# application-dev.yml - 开发环境
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/project_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: dev_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

logging:
  level:
    root: INFO
    com.example.project.mapper: DEBUG  # 开发环境打印 SQL
```

```yaml
# application-prod.yml - 生产环境
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL}          # 从环境变量读取，防止密码泄露！
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10

logging:
  level:
    root: WARN
    com.example.project: INFO
  file:
    path: /var/log/project/
```

### 4.2 敏感信息保护

```yaml
# ❌ 错误 - 明文密码直接写在配置中
spring:
  datasource:
    password: mySecret123

# ✅ 正确 - 通过环境变量引用
spring:
  datasource:
    password: ${DB_PASSWORD}

# ✅ 正确 - 通过配置文件加密（Jasypt）
spring:
  datasource:
    password: ENC(encrypted_password_here)

# ✅ 正确 - 使用配置中心（Nacos/Apollo/Consul）
```

### 4.3 日志配置 (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 文件输出（按日期切割） -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 分环境配置 -->
    <springProfile name="dev,test">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="com.example.project.mapper" level="DEBUG"/>
    </springProfile>

    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

---

## 五、常用依赖选型指南

### 5.1 ORM 框架

| 框架 | 适用场景 | 优势 | 劣势 |
|------|----------|------|------|
| **MyBatis** | 复杂 SQL、遗留数据库、精细化 SQL 控制 | SQL 完全可控，学习曲线低，XML/注解灵活 | 需手写 SQL，简单 CRUD 需重复编写 |
| **MyBatis-Plus** | 快速开发、标准 CRUD 场景 | 内置通用 CRUD，分页插件，代码生成器 | 重量级，复杂查询仍需 XML |
| **Spring Data JPA** | 新项目、简单 CRUD 为主 | 零 SQL 基础操作，方法命名查询，自动建表 | 复杂查询难优化，学习 Hibernate 成本高 |
| **JOOQ** | 类型安全的 SQL 构建 | 编译期 SQL 校验，代码自动生成 | 付费，国内使用少 |

**推荐组合（当前项目）**：`mybatis-spring-boot-starter` + `pagehelper-spring-boot-starter`

```xml
<!-- MyBatis -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>

<!-- 分页插件 -->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

### 5.2 安全框架

| 框架 | 适用场景 | 复杂度 |
|------|----------|--------|
| **Spring Security** | 企业级应用、OAuth2/OIDC、复杂 RBAC | 高 |
| **Shiro** | 中小型项目、简单权限控制 | 低 |
| **JJWT / Nimbus JOSE** | 纯 JWT 无状态认证 | 中 |
| **Sa-Token** | 国产轻量级，注解驱动权限控制 | 低 |

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT（与 Spring Security 配合） -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
```

### 5.3 缓存

| 框架 | 适用场景 | 说明 |
|------|----------|------|
| **Caffeine** | 单机应用，本地缓存 | 高性能，无需外部依赖，适合热点数据 |
| **Redis** | 分布式应用，共享缓存，Session 管理 | 持久化，丰富的数据结构，分布式锁 |
| **Spring Cache** | 注解驱动的抽象缓存层 | 可自由切换底层实现（Caffeine/Redis） |

```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Caffeine (本地缓存，无需外部依赖) -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### 5.4 消息队列

| 框架 | 适用场景 | 说明 |
|------|----------|------|
| **RabbitMQ** | 业务异步解耦、延迟消息、削峰填谷 | 功能完整，Spring AMQP 集成成熟 |
| **Kafka** | 大数据流处理、日志收集、事件溯源 | 高吞吐，适合数据密集型场景 |
| **RocketMQ** | 阿里系技术栈、事务消息 | 国产，社区活跃 |

### 5.5 API 文档

| 框架 | 说明 |
|------|------|
| **SpringDoc OpenAPI** | Spring Boot 3.x 推荐，Swagger 3 标准 |
| **Knife4j** | 基于 SpringDoc 的增强 UI，国内用户友好 |

```xml
<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

### 5.6 对象映射

| 框架 | 说明 |
|------|------|
| **MapStruct** | 编译期生成映射代码，性能最优，推荐首选 |
| **ModelMapper** | 反射实现，灵活但性能较低 |
| **手动转换** | 小项目直接用 Converter 工具类，避免依赖 |

```xml
<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.3</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.6.3</version>
    <scope>provided</scope>
</dependency>
```

### 5.7 常用工具库

```xml
<!-- 参数校验 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- AOP 支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- 工具类增强（Hutool） -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.28</version>
</dependency>

<!-- JSON 处理（Fastjson2 / Jackson） -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.51</version>
</dependency>

<!-- 数据库迁移 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

## 六、开发最佳实践

### 6.1 DTO / VO / Entity 分离

```java
// Entity - 数据库实体，与表一一对应
@Data
@TableName("user")  // MyBatis-Plus 注解
public class User {
    private Long id;
    private String username;
    private String password;     // 密码在 Entity 中存在
    private String email;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Request DTO - 入参，包含校验注解
@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}

// Response DTO - 出参，绝不包含敏感字段
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;       // 没有 password！
    private Integer status;
    private String statusDesc;  // 组合字段
    private LocalDateTime createdAt;
}

// VO - 视图对象（可选，前端展示用）
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String email;
    private Integer borrowCount;   // 跨实体聚合字段
    private Integer overdueCount;  // 跨实体聚合字段
}
```

### 6.2 参数校验

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // 单个对象校验
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        // ...
    }

    // 路径参数校验（需在启动类加 @Validated 或在类上加）
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(
        @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        // ...
    }

    // 嵌套校验 - 在集合字段上加 @Valid
    @PostMapping("/batch")
    public Result<Void> batchCreate(
        @Valid @NotEmpty(message = "列表不能为空") @RequestBody List<UserCreateRequest> requests) {
        // ...
    }
}
```

**在请求 DTO 中使用分组校验（可选）：**

```java
// 定义分组接口
public interface CreateGroup {}
public interface UpdateGroup {}

@Data
public class UserRequest {
    @NotNull(groups = UpdateGroup.class, message = "ID 不能为空")
    private Long id;

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "用户名不能为空")
    private String username;
}

// Controller 中指定分组
@PostMapping
public Result<Long> create(@Validated(CreateGroup.class) @RequestBody UserRequest request) {}
```

### 6.3 事务管理

```java
@Service
public class OrderServiceImpl implements OrderService {

    // ✅ 推荐：精确控制事务
    @Transactional(rollbackFor = Exception.class)  // 任何异常都回滚
    public void createOrder(OrderCreateRequest request) {
        // 1. 扣减库存
        // 2. 创建订单
        // 3. 创建支付记录
    }

    // ✅ 只读事务（查询操作，性能优化）
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        // ...
    }

    // ❌ 错误：事务范围过大（包含外部调用、文件操作等）
    @Transactional
    public void processOrder(Long id) {
        sendEmail();     // 发邮件 - 非数据库操作
        updateDb();      // 数据库操作
        uploadFile();    // 文件上传 - 非数据库操作
    }

    // ✅ 正确：通过编程式事务精确控制
    public void processOrder(Long id) {
        sendEmail();  // 事务外
        // 编程式事务
        transactionTemplate.execute(status -> {
            updateDb();
            return null;
        });
        uploadFile();  // 事务外
    }
}
```

**事务传播行为选择：**

| 传播行为 | 说明 | 使用场景 |
|----------|------|----------|
| `REQUIRED`（默认） | 有事务则加入，无则新建 | 大多数场景 |
| `REQUIRES_NEW` | 总是新建独立事务 | 日志记录、审计（失败不影响主事务） |
| `NESTED` | 嵌套事务，支持 Savepoint | 批量处理中单条失败不影响其他 |

### 6.4 全局异常处理

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(422, message);
    }

    // 未知异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "服务器内部错误");
    }
}
```

### 6.5 对象转换

**方案一：MapStruct（推荐）**

```java
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserResponse toResponse(User user);

    User toEntity(UserCreateRequest request);

    @Mapping(target = "statusDesc", expression = "java(user.getStatus().getDescription())")
    UserProfileVO toProfileVO(User user);

    List<UserResponse> toResponseList(List<User> users);
}
```

**方案二：手动工具类（无依赖）**

```java
public class UserConverter {

    public static UserResponse toResponse(User user) {
        if (user == null) return null;
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
```

### 6.6 统一响应格式

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>builder().code(200).message("success").build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(200).message("success").data(data).build();
    }

    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder().code(code).message(message).build();
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .build();
    }
}
```

---

## 七、项目初始化 Checklist

- [ ] 使用 [Spring Initializr](https://start.spring.io/) 或 IDEA 创建项目
- [ ] 启动类 `XxxApplication.java` 位于根包下
- [ ] 配置文件按环境拆分（dev / test / prod），敏感信息使用环境变量
- [ ] 配置 `logback-spring.xml` 或 `application.yml` 中的日志设置
- [ ] `@MapperScan` 已配置在启动类（MyBatis）或 Mapper 接口添加 `@Mapper`
- [ ] 全局异常处理（`@RestControllerAdvice`）已添加
- [ ] CORS 跨域配置已处理（开发阶段允许跨域）
- [ ] 统一响应格式 `Result<T>` 已实现
- [ ] 分页查询工具（PageHelper 或手写）已就绪
- [ ] 参数校验 `spring-boot-starter-validation` 已引入
- [ ] `.gitignore` 已配置（忽略 `target/`、`*.log`、`application-*.yml` 中的敏感配置）
- [ ] API 文档（SpringDoc / Knife4j）已配置
- [ ] 数据库迁移工具（Flyway / Liquibase）已配置（推荐）

---

## 八、常见反模式（避免）

| 反模式 | 说明 | 正确做法 |
|--------|------|----------|
| 胖 Controller | Controller 中写满业务逻辑 | 逻辑下沉到 Service |
| Entity 直接返回前端 | 暴露数据库字段、密码等 | 使用 DTO/VO 转换 |
| Service 中直接操作 HttpServletRequest | 破坏分层和可测试性 | 在 Controller 中提取参数 |
| 循环依赖 | Service A 依赖 B，B 又依赖 A | 抽取公共逻辑到新 Service，或用 `@Lazy` |
| 大事务 | 一个 `@Transactional` 包含外部调用 | 事务范围最小化，非关键操作放事务外 |
| 接口爆炸 | 每个功能都创建一个新接口 | 相关功能聚合到一个接口 |
| SQL 拼接 | 字符串拼接 SQL（注入风险） | 使用 MyBatis 的 `#{}` 参数化查询 |
| 硬编码 | 魔法数字、固定字符串散落代码 | 使用常量类或枚举 |

---

## 九、常用命令速查

```bash
# 项目编译
mvn clean compile

# 运行测试
mvn test

# 跳过测试打包
mvn clean package -DskipTests

# 本地运行（dev 环境）
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 运行指定 JAR
java -jar target/project-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 代码格式化检查（需集成 spotless 或 checkstyle）
mvn spotless:check
```
