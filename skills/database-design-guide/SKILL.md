---
name: database-design-guide
description: MySQL 数据库设计规范 —— 涵盖命名规范、字段类型选择、索引优化、范式设计、字符集配置、版本管理及通用表模板，适用于 Spring Boot + MyBatis 技术栈项目。
license: MIT
---

# MySQL 数据库设计规范

> 适用于 BookNexus 图书管理系统及其他 Spring Boot + MyBatis + MySQL 技术栈项目的数据库设计指导。

---

## 一、数据库设计流程

### 1.1 需求实体分析

1. **阅读需求文档**，提取所有名词和业务概念，识别核心实体。
2. **定义实体属性**：为每个实体列出其固有属性（如用户：用户名、密码、邮箱、手机号）。
3. **梳理实体关系**：
   | 关系类型 | 示例 |
   |----------|------|
   | 1:1 | 用户 ↔ 用户详情 |
   | 1:N | 用户 → 借阅记录 |
   | M:N | 用户 ↔ 角色（需中间表） |

### 1.2 E-R 建模

1. 使用工具（Draw.io / PlantUML / Navicat 模型）绘制 E-R 图。
2. 标注实体属性、主键、外键关系。
3. 确认 M:N 关系是否引入中间关联表。

### 1.3 表结构设计

1. 将 E-R 图中的实体转化为数据表。
2. 确定每个字段的数据类型、是否允许 NULL、默认值。
3. 为表添加通用审计字段（见第七节）。
4. 确定主键策略（自增主键 / UUID / 雪花ID）。

### 1.4 索引优化

1. 分析核心查询 SQL，为 WHERE、JOIN、ORDER BY、GROUP BY 列建立索引。
2. 使用 `EXPLAIN` 分析执行计划，避免全表扫描。
3. 遵循"最左前缀"原则设计联合索引。

---

## 二、命名规范

### 2.1 通用原则

- 所有数据库对象名称使用**小写字母 + 下划线**（snake_case）。
- 名称应**见名知意**，避免拼音、缩写（除非是业界通用缩写如 `id`、`url`）。
- 长度控制在 64 字符以内。
- 禁止使用 MySQL 保留字作为名称。

### 2.2 数据库名

```
规范：{项目名}_{环境后缀}
示例：book_nexus_dev、book_nexus_prod
```

### 2.3 表名

```
规范：小写 + 下划线，使用单数名词形式
正例：user、book、borrow_record、book_category
反例：Users、books_table、borrowRecord
```

关联表（M:N 中间表）命名：

```
规范：{表A}_{表B}，按字母顺序排列
正例：user_role、book_label
```

### 2.4 字段名

```
规范：小写 + 下划线，描述性名称
正例：user_name、created_at、is_deleted、category_id
反例：uname、ctime、isdelete、categoryId
```

**常用字段后缀约定**：

| 后缀 | 含义 | 示例 |
|------|------|------|
| `_id` | 外键关联 | `user_id`、`book_id` |
| `_at` | 时间戳 | `created_at`、`updated_at`、`returned_at` |
| `_by` | 操作人 | `created_by`、`approved_by` |
| `_count` | 数量 | `borrow_count`、`stock_count` |
| `_status` | 状态标识 | `order_status`、`borrow_status` |
| `is_` | 布尔值(0/1) | `is_deleted`、`is_enabled` |
| `_time` | 日期（无时分秒） | `birth_time`、`borrow_time` |

### 2.5 索引名

```
主键索引：pk_{表名}
唯一索引：uk_{表名}_{字段名}
普通索引：idx_{表名}_{字段名}
联合索引：idx_{表名}_{字段1}_{字段2}

示例：
pk_user
uk_user_email
idx_book_category_id
idx_borrow_record_user_id_book_id
```

### 2.6 外键名

```
规范：fk_{从表}_{主表}
示例：fk_borrow_record_user、fk_borrow_record_book
```

> **注意**：生产环境通常不使用物理外键，由应用层保证数据一致性。外键命名主要用于文档和设计阶段。

---

## 三、字段类型选择指南

### 3.1 整型

| 类型 | 范围 | 存储 | 推荐场景 |
|------|------|------|----------|
| `TINYINT` | -128~127 | 1 字节 | 状态标识、布尔值(0/1)、枚举值 |
| `SMALLINT` | -32768~32767 | 2 字节 | 数量较少的数据统计 |
| `MEDIUMINT` | -838万~838万 | 3 字节 | 中等范围计数值 |
| `INT` | -21亿~21亿 | 4 字节 | 常规 ID（较少数据量） |
| `BIGINT` | -2^63~2^63-1 | 8 字节 | **推荐所有主键 ID** |

> **推荐**：所有主键统一使用 `BIGINT UNSIGNED AUTO_INCREMENT`，避免后续数据增长导致的溢出问题。

```sql
-- 推荐写法
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY
```

### 3.2 字符串

| 类型 | 特点 | 推荐场景 |
|------|------|----------|
| `VARCHAR(N)` | 变长，最大 65535 字节 | 用户名、邮箱、标题等可变长文本 |
| `CHAR(N)` | 定长，N≤255 | 固定长度的编码、状态码、MD5 值 |
| `TEXT` | 大文本，最大 65535 字符 | 文章内容、简介 |
| `MEDIUMTEXT` | 更大文本，最大 1600 万字符 | 长文章、JSON 归档 |

```sql
-- 推荐长度标准
username    VARCHAR(50)      -- 用户名
email       VARCHAR(100)     -- 邮箱
phone       VARCHAR(20)      -- 手机号
password    VARCHAR(255)     -- 加密后的密码
title       VARCHAR(200)     -- 标题
description VARCHAR(500)     -- 简短描述
content     TEXT             -- 长文本内容
avatar_url  VARCHAR(500)     -- 头像/图片 URL
```

### 3.3 时间类型

| 类型 | 范围 | 精度 | 推荐场景 |
|------|------|------|----------|
| `DATETIME` | 1000~9999 年 | 秒/微秒 | **推荐**所有时间字段（审计字段、操作时间） |
| `TIMESTAMP` | 1970~2038 年 | 秒/微秒 | 记录自动更新（`ON UPDATE CURRENT_TIMESTAMP`） |
| `DATE` | 年-月-日 | 天 | 出生日期、活动日期 |
| `TIME` | 时:分:秒 | 秒 | 营业时间、耗时 |

```sql
-- 推荐使用 DATETIME(3)，保留毫秒精度
created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
```

> **注意**：优先使用 `DATETIME` 而非 `TIMESTAMP`，避免 2038 年溢出问题，且 `DATETIME` 不受时区影响。

### 3.4 枚举与状态

**不推荐使用 MySQL 原生 `ENUM` 类型**（ORDER BY 异常、扩展困难），推荐使用 `TINYINT` 或 `VARCHAR` + 代码枚举：

```sql
-- 推荐：TINYINT + 注释说明
status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 2-禁用, 3-注销'

-- 或 VARCHAR（可读性更好，但存储稍大）
borrow_status VARCHAR(20) NOT NULL DEFAULT 'BORROWED' COMMENT '借阅状态: BORROWED/RETURNED/OVERDUE'
```

### 3.5 布尔值

```sql
-- 使用 TINYINT(1)，0 表示 false，1 表示 true
is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除'
is_enabled  TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用, 1-启用'
```

### 3.6 金额

**禁止使用 `FLOAT`/`DOUBLE`**（精度丢失），使用 `DECIMAL(M, D)`：

```sql
price       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价（元）'
total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额（元）'
fine_amount  DECIMAL(8, 2) NOT NULL DEFAULT 0.00 COMMENT '罚款金额（元）'
```

| 场景 | 推荐精度 |
|------|----------|
| 单价 | `DECIMAL(10, 2)` - 最大 99999999.99 |
| 订单金额 | `DECIMAL(12, 2)` - 最大 9999999999.99 |
| 汇率/利率 | `DECIMAL(10, 6)` - 保留 6 位小数 |

### 3.7 JSON 字段

MySQL 5.7+ 支持原生 `JSON` 类型，适用于扩展字段、配置信息：

```sql
-- 适用场景：个性化标签、扩展配置、元数据
extra_info JSON COMMENT '扩展信息（JSON 格式）'

-- 查询示例
SELECT * FROM user WHERE JSON_EXTRACT(extra_info, '$.vip_level') = 3;
SELECT * FROM user WHERE extra_info->>'$.vip_level' = '3';
```

> **限制**：JSON 字段不推荐作为核心查询条件（无法使用索引，除非使用虚拟列+索引）。

### 3.8 类型选择速查表

| 业务场景 | 推荐类型 |
|----------|----------|
| 主键 ID | `BIGINT UNSIGNED AUTO_INCREMENT` |
| 用户名/昵称 | `VARCHAR(50)` |
| 邮箱 | `VARCHAR(100)` |
| 手机号 | `VARCHAR(20)` |
| 密码（加密后） | `VARCHAR(255)` |
| 标题 | `VARCHAR(200)` |
| 描述/摘要 | `VARCHAR(500)` |
| 文章内容 | `TEXT` / `MEDIUMTEXT` |
| 头像/图片 URL | `VARCHAR(500)` |
| 点赞数/收藏数 | `INT UNSIGNED DEFAULT 0` |
| 金额 | `DECIMAL(10, 2)` |
| 状态标识 | `TINYINT` 或 `VARCHAR(20)` |
| 布尔标识 | `TINYINT(1)` |
| 创建/更新时间 | `DATETIME(3)` |
| 日期（天） | `DATE` |
| IP 地址 | `VARCHAR(45)`（兼容 IPv6） |
| 配置/元数据 | `JSON` |

---

## 四、索引设计与优化

### 4.1 索引类型

| 索引类型 | 关键字 | 特点 |
|----------|--------|------|
| 主键索引 | `PRIMARY KEY` | 唯一且非空，每表只能有一个 |
| 唯一索引 | `UNIQUE` | 值唯一，允许 NULL |
| 普通索引 | `INDEX` | 加速查询 |
| 全文索引 | `FULLTEXT` | 文本搜索，仅 InnoDB(5.6+)/MyISAM |
| 联合索引 | `INDEX(col1, col2)` | 多列组合索引 |

### 4.2 联合索引设计原则

1. **最左前缀原则**：联合索引 `(a, b, c)` 相当于创建了 `(a)`、`(a, b)`、`(a, b, c)` 三个索引。
2. **区分度高的字段放最左**：如 `status` 只有少量取值，不适合放最左列。
3. **把等值查询的字段放前面**，范围查询的字段放后面：

```sql
-- 查询：WHERE user_id = ? AND status = ? AND created_at BETWEEN ? AND ?
-- 等值字段放前，范围字段放后
INDEX idx_xxx (user_id, status, created_at)
```

4. **覆盖索引**：当查询的所有列都在索引中时，无需回表：

```sql
-- 查询：SELECT user_id, book_id, borrowed_at FROM borrow_record WHERE user_id = ?
-- 如果索引包含所有查询列，则为覆盖索引
INDEX idx_borrow_record_user_book_time (user_id, book_id, borrowed_at)
```

### 4.3 EXPLAIN 分析

```sql
EXPLAIN SELECT * FROM borrow_record WHERE user_id = 1 AND status = 'BORROWED';
```

**关键字段解读**：

| 字段 | 含义 | 期望值 |
|------|------|--------|
| `type` | 访问类型 | `const` > `ref` > `range` > `index` > `ALL` |
| `key` | 实际使用的索引 | 非 NULL |
| `rows` | 扫描行数 | 越小越好 |
| `Extra` | 额外信息 | `Using index`（覆盖索引）最佳；避免 `Using filesort`、`Using temporary` |

### 4.4 索引使用规范

```sql
-- ✅ 推荐：等值查询时，联合索引字段顺序随意（优化器自动调整）
SELECT * FROM t WHERE a = 1 AND b = 2 AND c = 3;  -- 可使用 idx(a,b,c)

-- ❌ 违反最左前缀：缺少前导列 a
SELECT * FROM t WHERE b = 2 AND c = 3;

-- ❌ 索引失效：在索引列上使用函数
SELECT * FROM user WHERE DATE(created_at) = '2024-01-01';

-- ✅ 正确写法
SELECT * FROM user WHERE created_at >= '2024-01-01' AND created_at < '2024-01-02';

-- ❌ 索引失效：前置模糊匹配
SELECT * FROM book WHERE title LIKE '%Spring%';

-- ✅ 可走索引（前缀匹配）
SELECT * FROM book WHERE title LIKE 'Spring%';
```

### 4.5 慢查询优化

1. 开启慢查询日志：

```sql
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1;  -- 超过 1 秒的查询记录
```

2. 使用 `mysqldumpslow` 分析：

```bash
mysqldumpslow -s t -t 10 /var/log/mysql/slow.log  # 耗时最长的 10 条
```

3. 分析锁等待：

```sql
SHOW ENGINE INNODB STATUS;
SELECT * FROM information_schema.INNODB_TRX;
```

---

## 五、表设计范式与反范式权衡

### 5.1 三大范式

| 范式 | 规则 | 示例 |
|------|------|------|
| 1NF | 字段不可再分，每个字段原子化 | 地址应拆分为省、市、区，而非存一列 |
| 2NF | 非主键字段完全依赖于主键（消除部分依赖） | 联合主键 `(order_id, product_id)` 时，`product_name` 只依赖 `product_id`，应拆分 |
| 3NF | 非主键字段不传递依赖于主键（消除传递依赖） | `city_name` 通过 `city_id` 传递依赖于主键，应拆出城市表 |

### 5.2 范式设计的优缺点

| | 优点 | 缺点 |
|------|------|------|
| 完全范式化 | 数据冗余小、更新异常少、数据一致性好 | 多表 JOIN 查询性能差 |
| 完全反范式化 | 查询速度快 | 数据冗余大、更新复杂度高 |

### 5.3 实际项目建议

**优先遵循 3NF，根据查询场景适度反范式**：

```sql
-- ✅ 范式设计（3NF）：借阅表只存 book_id
CREATE TABLE borrow_record (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    book_id     BIGINT UNSIGNED NOT NULL,
    borrowed_at DATETIME(3) NOT NULL,
    due_at      DATETIME(3) NOT NULL,
    returned_at DATETIME(3) DEFAULT NULL
);

-- ✅ 适度反范式：冗余常用查询字段，避免 JOIN
CREATE TABLE borrow_record (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    user_name   VARCHAR(50) NOT NULL,           -- 冗余用户名
    book_id     BIGINT UNSIGNED NOT NULL,
    book_title  VARCHAR(200) NOT NULL,           -- 冗余书名
    book_isbn   VARCHAR(20) NOT NULL,            -- 冗余 ISBN
    borrowed_at DATETIME(3) NOT NULL,
    due_at      DATETIME(3) NOT NULL
);
```

**反范式时机判断**：
- 该字段在查询时高频使用 → 可冗余
- 该字段极少变更 → 可冗余
- 该字段变更后，对数据一致性要求不高 → 可冗余
- 反范式的字段变更后，需同步更新冗余数据 → 业务层需配合

---

## 六、数据库版本管理

### 6.1 Flyway vs Liquibase

| 维度 | Flyway | Liquibase |
|------|--------|-----------|
| 迁移文件格式 | SQL（推荐）或 Java | XML / YAML / JSON / SQL |
| 学习成本 | 低 | 中 |
| Spring Boot 集成 | `flyway-core` 自动配置 | `liquibase-core` 自动配置 |
| 版本命名 | `V{version}__{description}.sql` | 在 changelog 中声明 |
| 回滚支持 | 社区版不支持（需手动回滚） | 支持（需手动编写回滚语句） |
| 社区活跃度 | 高 | 高 |
| 推荐场景 | 简单直接、SQL 驱动型团队 | 多数据库兼容、需要内置回滚 |

### 6.2 Flyway 推荐配置（BookNexus 默认方案）

**Maven 依赖**（已在 Spring Boot Starter 中管理）：

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

**目录结构**：

```
src/main/resources/db/migration/
├── V1__init_schema.sql          # 初始表结构
├── V2__add_admin_user.sql       # 管理员用户数据
├── V3__add_book_category.sql    # 图书分类表
├── V4__add_borrow_record.sql    # 借阅记录表
└── V5__add_user_avatar.sql     # 用户头像字段
```

**application.yml 配置**：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true      # 非空数据库首次迁移
    table: flyway_schema_history   # 迁移记录表名
    encoding: UTF-8
```

**命名规范**：

```
V{版本号}__{描述}.sql
版本号：V1、V2、V3... 或 V1.0.0、V1.0.1...
描述：小写英文 + 下划线（snake_case）

正例：V1__init_schema.sql、V3__add_borrow_record.sql
反例：V1_create_table.sql（单下划线）、V3_add-borrow-record.sql（使用连字符）
```

### 6.3 Liquibase 配置（备选方案）

```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/changelog/v1.0.0/init-schema.yaml
  - include:
      file: db/changelog/v1.0.1/add-borrow-record.yaml
```

### 6.4 版本管理最佳实践

1. **每次 DDL 变更务必创建新的迁移文件**，禁止直接修改已执行的旧迁移文件。
2. **迁移脚本必须可重复执行**（使用 `CREATE TABLE IF NOT EXISTS` 或先 `DROP` 再 `CREATE`）。
3. **每个迁移文件应只包含一个逻辑变更**，便于问题定位和回滚。
4. **迁移脚本纳入 Git 版本控制**，与代码同步提交。
5. **生产环境迁移前在开发/测试环境完整验证**。

---

## 七、通用审计字段规范

### 7.1 每张表必须包含的字段

```sql
-- 基础审计字段（所有表必须）
id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
is_deleted  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除'
```

### 7.2 可选审计字段

```sql
created_by  BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
updated_by  BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID',
deleted_at  DATETIME(3) DEFAULT NULL COMMENT '删除时间',
deleted_by  BIGINT UNSIGNED DEFAULT NULL COMMENT '删除人ID',
version     INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
```

### 7.3 逻辑删除说明

- 所有删除操作使用**逻辑删除**（`is_deleted = 1`），不物理删除数据。
- 查询时必须加上 `AND is_deleted = 0` 条件。
- MyBatis-Plus 可通过 `@TableLogic` 注解自动处理：

```java
@TableLogic
private Integer isDeleted;
```

### 7.4 乐观锁（可选）

高并发场景下使用 `version` 字段实现乐观锁：

```java
UPDATE book SET stock = stock - 1, version = version + 1
WHERE id = ? AND version = ? AND stock > 0;
```

若 `affected rows = 0` 则说明版本冲突或库存不足，需要重试或提示用户。

---

## 八、字符集与排序规则

### 8.1 字符集选择

**统一使用 `utf8mb4`**（MySQL 真正的 UTF-8 实现，支持 emoji 和扩展字符）：

```sql
-- 建库时指定
CREATE DATABASE book_nexus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 建表时指定
CREATE TABLE user (
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **禁止使用 `utf8`**：MySQL 中的 `utf8` 是 `utf8mb3` 的别名，只支持 3 字节 UTF-8，无法存储 emoji 和部分中文扩展字符。

### 8.2 排序规则选择

| 排序规则 | 特点 | 推荐场景 |
|----------|------|----------|
| `utf8mb4_unicode_ci` | Unicode 标准比较，准确但稍慢 | **通用推荐** |
| `utf8mb4_general_ci` | 简化比较，快但不够准确 | 性能优先场景 |
| `utf8mb4_bin` | 二进制比较，区分大小写 | 严格区分大小写的字段（如验证码） |
| `utf8mb4_0900_ai_ci` | MySQL 8.0+ 默认，基于 Unicode 9.0，不区分重音 | **MySQL 8.0+ 推荐** |

```sql
-- MySQL 8.0+ 推荐配置
CREATE DATABASE book_nexus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

### 8.3 连接配置

Spring Boot 数据源 URL 必须指定字符集：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/book_nexus?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
```

---

## 九、通用表设计模板

### 9.1 用户表

```sql
CREATE TABLE user (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '加密密码',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar_url      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    nickname        VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    gender          TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    birth_date      DATE DEFAULT NULL COMMENT '出生日期',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 2-禁用, 3-注销',
    last_login_at   DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_status (status),
    INDEX idx_user_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 9.2 角色表

```sql
CREATE TABLE role (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name       VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50) NOT NULL COMMENT '角色编码（如 ROLE_ADMIN）',
    description     VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    sort_order      INT NOT NULL DEFAULT 0 COMMENT '排序',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0,

    UNIQUE KEY uk_role_code (role_code),
    INDEX idx_role_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
```

### 9.3 用户角色关联表（M:N）

```sql
CREATE TABLE user_role (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id         BIGINT UNSIGNED NOT NULL COMMENT '角色ID',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user_id (user_id),
    INDEX idx_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
```

### 9.4 权限表

```sql
CREATE TABLE permission (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    resource_type   VARCHAR(50) DEFAULT NULL COMMENT '资源类型',
    resource_path   VARCHAR(200) DEFAULT NULL COMMENT '资源路径',
    method          VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    parent_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '父权限ID',
    sort_order      INT NOT NULL DEFAULT 0 COMMENT '排序',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0,

    UNIQUE KEY uk_permission_code (permission_code),
    INDEX idx_permission_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
```

### 9.5 角色权限关联表

```sql
CREATE TABLE role_permission (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id         BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id   BIGINT UNSIGNED NOT NULL COMMENT '权限ID',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_permission_role (role_id),
    INDEX idx_role_permission_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
```

### 9.6 操作日志表

```sql
CREATE TABLE operation_log (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id         BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
    username        VARCHAR(50) DEFAULT NULL COMMENT '操作用户名（冗余）',
    module          VARCHAR(100) NOT NULL COMMENT '操作模块',
    action          VARCHAR(100) NOT NULL COMMENT '操作动作',
    target_type     VARCHAR(50) DEFAULT NULL COMMENT '操作对象类型',
    target_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '操作对象ID',
    request_uri     VARCHAR(500) DEFAULT NULL COMMENT '请求URI',
    request_method  VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    request_params  JSON DEFAULT NULL COMMENT '请求参数（JSON）',
    request_body    JSON DEFAULT NULL COMMENT '请求体（JSON）',
    ip_address      VARCHAR(45) DEFAULT NULL COMMENT '请求IP',
    user_agent      VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    execution_time  INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态: 1-成功, 2-失败',
    error_message   TEXT DEFAULT NULL COMMENT '错误信息',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
    INDEX idx_log_user_id (user_id),
    INDEX idx_log_module (module),
    INDEX idx_log_created_at (created_at),
    INDEX idx_log_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
```

### 9.7 系统参数/字典表

```sql
CREATE TABLE sys_dict (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_type       VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code       VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_label      VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value      VARCHAR(200) DEFAULT NULL COMMENT '字典值',
    description     VARCHAR(200) DEFAULT NULL COMMENT '描述',
    sort_order      INT NOT NULL DEFAULT 0 COMMENT '排序',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',

    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted      TINYINT(1) NOT NULL DEFAULT 0,

    INDEX idx_dict_type (dict_type),
    UNIQUE KEY uk_dict_type_code (dict_type, dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';
```

---

## 十、建表规范 Checklist

- [ ] 表名、字段名使用小写 + 下划线（snake_case）
- [ ] 主键使用 `BIGINT UNSIGNED AUTO_INCREMENT`
- [ ] 包含 `id`、`created_at`、`updated_at`、`is_deleted` 审计字段
- [ ] 存储引擎使用 `InnoDB`
- [ ] 字符集使用 `utf8mb4`，排序规则使用 `utf8mb4_unicode_ci` 或 `utf8mb4_0900_ai_ci`
- [ ] 每个字段添加 `COMMENT` 注释
- [ ] 金额字段使用 `DECIMAL`，禁止 `FLOAT`/`DOUBLE`
- [ ] 时间字段使用 `DATETIME`，禁止字符串存时间
- [ ] 布尔字段使用 `TINYINT(1)`，命名以 `is_` 前缀
- [ ] 外键列建立索引
- [ ] WHERE/JOIN/ORDER BY 列建立索引，使用 `EXPLAIN` 验证
- [ ] 避免在索引列上使用函数
- [ ] 使用 Flyway 管理数据库版本（或 Liquibase）
- [ ] DDL 脚本可重复执行

---

## 参考资源

- [MySQL 8.4 Reference Manual - Data Types](https://dev.mysql.com/doc/refman/8.4/en/data-types.html)
- [MySQL 8.4 Reference Manual - Optimization](https://dev.mysql.com/doc/refman/8.4/en/optimization.html)
- [MySQL 8.4 Reference Manual - Character Sets](https://dev.mysql.com/doc/refman/8.4/en/charset.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Liquibase Documentation](https://docs.liquibase.com/)
