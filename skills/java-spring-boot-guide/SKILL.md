---
name: java-spring-boot-guide
description: Spring Boot + MyBatis + MySQL 技术栈开发规范与最佳实践，涵盖项目结构、RESTful API 设计、分层架构、安全配置与性能优化。
license: MIT
---

# Java Spring Boot 开发指南

> 适用于 BookNexus 图书管理系统及其他 Spring Boot + MyBatis + MySQL 技术栈项目。

---

## 一、项目结构规范

### 1.1 标准分层架构

```
src/main/java/com/booknexus/
├── controller/          # 控制层：处理请求、参数校验、返回响应
├── service/             # 业务层：核心业务逻辑、事务管理
│   └── impl/            # 业务实现类
├── mapper/              # 数据访问层：MyBatis Mapper 接口
├── entity/              # 实体类：与数据库表一一对应
├── dto/                 # 数据传输对象
│   ├── request/         # 请求 DTO
│   └── response/        # 响应 DTO
├── vo/                  # 视图对象
├── config/              # 配置类：WebMvc、Security、MyBatis 等
├── interceptor/         # 拦截器
├── exception/           # 异常定义与处理
│   ├── global/          # 全局异常处理器
│   └── business/       # 业务异常
└── util/                # 工具类
```

### 1.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Controller | `XxxController` | `BookController`、`UserController` |
| Service 接口 | `IXxxService` | `IBookService`、`IUserService` |
| Service 实现 | `XxxServiceImpl` | `BookServiceImpl`、`UserServiceImpl` |
| Mapper | `XxxMapper` | `BookMapper`、`UserMapper` |
| 实体类 | 表名转驼峰 | `BorrowRecord`、`UserRole` |
| 请求 DTO | `XxxRequest` | `CreateBookRequest`、`LoginRequest` |
| 响应 DTO | `XxxResponse` | `BookResponse`、`UserResponse` |

---

## 二、RESTful API 设计规范

### 2.1 URL 命名规范

```
资源路径使用复数名词，小写 + 连字符
GET    /api/books              # 查询所有图书
GET    /api/books/{id}         # 查询单个图书
POST   /api/books              # 创建图书
PUT    /api/books/{id}         # 更新图书
DELETE /api/books/{id}         # 删除图书
GET    /api/users/{id}/borrows # 查询用户的借阅记录（嵌套资源）
```

### 2.2 响应格式统一

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

```java
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

### 2.3 HTTP 状态码使用

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | OK | 成功响应 |
| 201 | Created | 资源创建成功 |
| 204 | No Content | 删除成功（无返回体） |
| 400 | Bad Request | 参数校验失败 |
| 401 | Unauthorized | 未登录认证 |
| 403 | Forbidden | 无权限访问 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突（如重复创建） |
| 500 | Internal Server Error | 服务器内部错误 |

### 2.4 分页查询

```java
@GetMapping("/books")
public ApiResponse<PageResult<BookResponse>> getBooks(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword) {
    PageResult<BookResponse> result = bookService.pageBooks(page, size, keyword);
    return ApiResponse.success(result);
}
```

```java
@Data
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> records;
}
```

---

## 三、Controller 开发规范

### 3.1 基本结构

```java
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    @PostMapping
    public ApiResponse<Long> createBook(@RequestBody @Valid CreateBookRequest request) {
        Long id = bookService.createBook(request);
        return ApiResponse.success(id);
    }
}
```

### 3.2 参数校验

使用 `@Valid` + `BindingResult` 或全局异常处理 `@Validated`：

```java
@PostMapping
public ApiResponse<Long> createBook(@RequestBody @Valid CreateBookRequest request) {
    // @Valid 会自动校验，校验失败抛 MethodArgumentNotValidException
    Long id = bookService.createBook(request);
    return ApiResponse.success(id);
}
```

全局异常处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }
}
```

常用校验注解：

| 注解 | 作用 | 示例 |
|------|------|------|
| `@NotNull` | 不能为 null | `@NotNull(message = "ID不能为空")` |
| `@NotBlank` | 不能为空字符串 | 校验字符串 |
| `@NotEmpty` | 不能为空（集合/数组） | 校验集合 |
| `@Size` | 长度范围 | `@Size(min = 2, max = 50)` |
| `@Email` | 邮箱格式 | |
| `@Pattern` | 正则表达式 | `@Pattern(regexp = "^1[3-9]\\d{9}$")` |
| `@Min`/`@Max` | 数值范围 | `@Min(0) @Max(150)` |
| `@Range` | 范围（金额等） | |
| `@Valid` | 嵌套对象校验 | |

### 3.3 避免直接操作 Entity

**禁止在 Controller 直接返回 Entity**，必须通过 DTO/VO：

```java
// ❌ 禁止
@GetMapping("/{id}")
public Book getBook(@PathVariable Long id) {
    return bookMapper.selectById(id);
}

// ✅ 推荐
@GetMapping("/{id}")
public ApiResponse<BookResponse> getBook(@PathVariable Long id) {
    BookResponse book = bookService.getBookById(id);
    return ApiResponse.success(book);
}
```

---

## 四、Service 开发规范

### 4.1 接口与实现分离

```java
public interface IBookService {
    BookResponse getBookById(Long id);
    Long createBook(CreateBookRequest request);
    void updateBook(Long id, UpdateBookRequest request);
    void deleteBook(Long id);
    PageResult<BookResponse> pageBooks(int page, int size, String keyword);
}
```

```java
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookMapper bookMapper;
    private final BookCategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        return convertToResponse(book);
    }

    @Override
    @Transactional
    public Long createBook(CreateBookRequest request) {
        // 业务校验
        validateBookRequest(request);

        Book book = new Book();
        BeanUtils.copyProperties(request, book);
        bookMapper.insert(book);
        return book.getId();
    }
}
```

### 4.2 事务管理

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void batchCreateBooks(List<CreateBookRequest> requests) {
    for (CreateBookRequest request : requests) {
        Book book = new Book();
        BeanUtils.copyProperties(request, book);
        bookMapper.insert(book);
    }
}
```

> **注意**：
> - `@Transactional` 默认只对 `RuntimeException` 回滚，需指定 `rollbackFor = Exception.class`
> - `readOnly = true` 用于只读操作，性能更优
> - 避免在事务内调用外部服务或超时操作

### 4.3 业务异常处理

```java
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

---

## 五、Mapper（MyBatis）开发规范

### 5.1 Mapper 接口

```java
@Mapper
public interface BookMapper {

    Book selectById(@Param("id") Long id);

    List<Book> selectByCondition(@Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    int insert(Book book);

    int updateById(Book book);

    int deleteById(@Param("id") Long id);
}
```

### 5.2 XML 文件规范

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.booknexus.mapper.BookMapper">

    <resultMap id="BaseResultMap" type="com.booknexus.entity.Book">
        <id column="id" property="id"/>
        <result column="title" property="title"/>
        <result column="isbn" property="isbn"/>
        <!-- 避免全部使用 *，明确列出需要的列 -->
    </resultMap>

    <sql id="Base_Column_List">
        id, title, isbn, author, publisher, category_id,
        stock, available_stock, price, description,
        cover_url, created_at, updated_at
    </sql>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM book
        WHERE id = #{id} AND is_deleted = 0
    </select>

    <select id="selectByCondition" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM book
        WHERE is_deleted = 0
        <if test="keyword != null and keyword != ''">
            AND (title LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        ORDER BY created_at DESC
        LIMIT #{offset}, #{limit}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO book (title, isbn, author, publisher, category_id,
                          stock, available_stock, price, description, cover_url)
        VALUES (#{title}, #{isbn}, #{author}, #{publisher}, #{categoryId},
                #{stock}, #{availableStock}, #{price}, #{description}, #{coverUrl})
    </insert>

    <update id="updateById">
        UPDATE book
        SET title = #{title},
            author = #{author},
            updated_at = NOW(3)
        WHERE id = #{id} AND is_deleted = 0
    </update>

    <update id="deleteById">
        UPDATE book SET is_deleted = 1, updated_at = NOW(3)
        WHERE id = #{id}
    </update>

</mapper>
```

### 5.3 避免 N+1 查询

```xml
<!-- ❌ N+1 问题：循环内查询 -->
<foreach collection="bookIds" item="bookId">
    SELECT * FROM book WHERE id = #{bookId}
</foreach>

<!-- ✅ 批量查询 -->
<select id="selectByIds" resultMap="BaseResultMap">
    SELECT * FROM book WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

---

## 六、Entity 开发规范

### 6.1 基本结构

```java
@Data
@TableName("book")
public class Book {

    private Long id;
    private String title;
    private String isbn;
    private String author;
    private String publisher;
    private Long categoryId;
    private Integer stock;
    private Integer availableStock;
    private BigDecimal price;
    private String description;
    private String coverUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

### 6.2 避免 Entity 膨胀

- 不要在 Entity 中放入与数据库无关的字段
- 扩展字段使用 JSON 类型或单独表
- 不要在 Entity 中写业务逻辑

---

## 七、DTO 与转换规范

### 7.1 DTO 命名

| 类型 | 命名 | 用途 |
|------|------|------|
| 创建请求 | `CreateXxxRequest` | 新增数据 |
| 更新请求 | `UpdateXxxRequest` | 修改数据 |
| 详情请求 | `XxxDetailRequest` | 详情查询参数 |
| 单个响应 | `XxxResponse` | 返回单个对象 |
| 列表响应 | `XxxListResponse` | 返回列表（含分页） |
| 分页响应 | `PageResult<XxxResponse>` | 分页数据 |

### 7.2 对象转换

使用 MapStruct 或 BeanUtils：

```java
// 推荐 MapStruct，性能更好
@Mapper(componentModel = "spring")
public interface BookConverter {
    BookResponse toResponse(Book book);
    Book toEntity(CreateBookRequest request);
}

// 使用
BookResponse response = bookConverter.toResponse(book);
```

```java
// 简单场景可用 BeanUtils
BeanUtils.copyProperties(source, target);
```

---

## 八、安全配置规范

### 8.1 密码加密

使用 BCrypt 加密：

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// Service 中使用
String encodedPassword = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

### 8.2 接口权限控制

```java
@GetMapping("/admin/books")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<List<BookResponse>> getAllBooksForAdmin() {
    // 仅管理员可访问
}
```

### 8.3 敏感数据脱敏

```java
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;       // 脱敏：t***@example.com
    private String phone;       // 脱敏：138****5678
    private String idCard;      // 脱敏：110***********1234
}
```

### 8.4 防止 SQL 注入

- MyBatis 使用 `#{}` 禁止 `${}`
- 不拼接用户输入到 SQL

---

## 九、性能优化规范

### 9.1 数据库连接池

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 9.2 缓存使用

```java
@Service
@RequiredArgsConstructor
public class BookService {

    @Cacheable(value = "book", key = "#id")
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        // 缓存查询结果
    }

    @CacheEvict(value = "book", key = "#id")
    public void updateBook(Long id, UpdateBookRequest request) {
        // 更新时清除缓存
    }
}
```

### 9.3 异步处理

```java
@Async
public void sendNotification(Long userId, String message) {
    // 异步发送通知，不阻塞主流程
}
```

### 9.4 分页优化

```xml
<select id="selectPage" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM book
    WHERE is_deleted = 0
    ORDER BY id DESC
    LIMIT #{offset}, #{size}
</select>
```

---

## 十、日志规范

### 10.1 日志级别

| 级别 | 使用场景 |
|------|----------|
| ERROR | 异常信息，需要关注 |
| WARN | 潜在问题，如重试、熔断 |
| INFO | 业务流程节点（登录、关键操作） |
| DEBUG | 开发调试信息 |

### 10.2 日志内容

```java
// ✅ 推荐：包含关键业务信息
log.info("User {} logged in from IP {}", username, ip);

// ❌ 禁止：日志内容过于简单或缺失关键信息
log.info("User logged in");
log.debug("method executed");
```

### 10.3 敏感信息

禁止记录：`password`、`token`、`idCard`、`bankCard` 等敏感信息。

---

## 十一、配置管理

### 11.1 多环境配置

```
application.yml              # 主配置
application-dev.yml          # 开发环境
application-test.yml         # 测试环境
application-prod.yml         # 生产环境
```

```yaml
spring:
  profiles:
    active: dev
```

### 11.2 敏感配置

使用环境变量或配置中心，禁止硬编码：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/book_nexus}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
```

---

## 十二、代码审查 Checklist

- [ ] Controller 返回统一格式 `ApiResponse`，无直接返回 Entity
- [ ] 所有请求参数使用 `@Valid` 校验
- [ ] Service 层标注 `@Transactional`，指定 `rollbackFor`
- [ ] 避免在循环内查询数据库
- [ ] SQL 使用 `#{}`，禁止 `${}` 拼接
- [ ] 密码使用 BCrypt 加密存储
- [ ] 接口有适当的权限控制
- [ ] 日志包含关键业务信息，不记录敏感数据
- [ ] 分页查询有总数限制（MAX 1000）
- [ ] 接口有适当的超时处理

---

## 参考资源

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/zh/index.html)
- [MyBatis-Plus Documentation](https://baomidou.com/pages/24112f/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)