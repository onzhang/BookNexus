# BookNexus E-R 图

```mermaid
erDiagram
    user {
        bigint id PK
        varchar username
        varchar password
        varchar email
        varchar phone
        enum role "ADMIN | USER"
        enum status "ENABLED | DISABLED"
        varchar avatar_url
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    book {
        bigint id PK
        varchar title
        varchar author
        varchar isbn UK
        varchar publisher
        date publish_date
        text description
        varchar cover_url
        int stock
        int available_stock
        enum status "AVAILABLE | BORROWED | DAMAGED | LOST"
        bigint bookshelf_id FK
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    category {
        bigint id PK
        varchar name
        bigint parent_id FK "self-ref"
        int sort_order
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    borrow_record {
        bigint id PK
        bigint user_id FK
        bigint book_id FK
        date borrow_date
        date due_date
        date return_date
        enum status "PENDING | APPROVED | REJECTED | BORROWED | RENEWED | RETURNED"
        varchar reject_reason
        int renew_count
        decimal fine_amount
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    bookshelf {
        bigint id PK
        varchar name
        varchar location
        varchar description
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    book_category_rel {
        bigint id PK
        bigint book_id FK
        bigint category_id FK
    }

    favorite {
        bigint id PK
        bigint user_id FK
        bigint book_id FK
        datetime created_at
    }

    notification {
        bigint id PK
        bigint user_id FK
        enum type "SYSTEM | SUBSCRIPTION | OVERDUE"
        varchar title
        text content
        tinyint is_read
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    announcement {
        bigint id PK
        varchar title
        text content
        bigint publisher_id FK
        tinyint is_published
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    operation_log {
        bigint id PK
        varchar operator
        varchar action
        varchar target_type
        bigint target_id
        text detail
        varchar ip
        varchar result
        datetime created_at
    }

    subscription {
        bigint id PK
        bigint user_id FK
        bigint book_id FK
        tinyint is_active
        datetime created_at
        datetime updated_at
    }

    message {
        bigint id PK
        bigint user_id FK
        text content
        text reply
        datetime reply_at
        bigint replier_id FK
        datetime created_at
        datetime updated_at
        tinyint is_deleted
    }

    %% ---------- 用户核心关系 ----------
    user ||--o{ borrow_record : "借阅"
    user ||--o{ favorite : "收藏"
    user ||--o{ notification : "接收通知"
    user ||--o{ subscription : "订阅"
    user ||--o{ message : "留言"
    user ||--o{ announcement : "发布公告"

    %% ---------- 图书核心关系 ----------
    book ||--o{ borrow_record : "被借阅"
    book ||--o{ favorite : "被收藏"
    book ||--o{ subscription : "被订阅"
    book ||--o{ book_category_rel : "归属分类"
    book }o--|| bookshelf : "位于书架"

    %% ---------- 分类关系 ----------
    category ||--o{ book_category_rel : "包含图书"
    category ||--o{ category : "父子分类"

    %% ---------- 复合唯一约束 ----------
    favorite }o--|| book : ""
    favorite }o--|| user : ""
    subscription }o--|| book : ""
    subscription }o--|| user : ""
```

## 实体关系说明

| 关系 | 类型 | 说明 |
|------|------|------|
| user → borrow_record | **1:N** | 一个用户可拥有多条借阅记录 |
| user → favorite | **1:N** | 一个用户可收藏多本图书（user_id + book_id 联合唯一） |
| user → notification | **1:N** | 一个用户可接收多条通知（逾期/订阅/系统） |
| user → subscription | **1:N** | 一个用户可订阅多本图书（user_id + book_id 联合唯一） |
| user → message | **1:N** | 一个用户可提交多条留言 |
| user → announcement | **1:N** | 一个管理员可发布多条公告（publisher_id → user.id） |
| book → borrow_record | **1:N** | 一本书可被多次借阅 |
| book → favorite | **1:N** | 一本书可被多个用户收藏 |
| book → subscription | **1:N** | 一本书可被多个用户订阅 |
| book → bookshelf | **N:1** | 多本书可位于同一书架 |
| book → book_category_rel | **1:N** | 一本书可属于多个分类（M:N 中间表） |
| category → book_category_rel | **1:N** | 一个分类可包含多本图书 |
| category → category | **1:N** | 分类自引用树形结构（parent_id） |

### 关键设计点

1. **软删除**：除 `favorite`、`subscription`、`operation_log`、`book_category_rel` 外，其余表均采用 `is_deleted` 逻辑删除。
2. **联合唯一索引**：`favorite(user_id, book_id)` 和 `subscription(user_id, book_id)` 防止重复收藏/订阅。
3. **分类树**：`category` 通过 `parent_id` 自引用实现无限级树形分类。
4. **借阅状态机**：`borrow_record.status` 流转路径为 PENDING → APPROVED → BORROWED → (RENEWED → BORROWED) → RETURNED，或 PENDING → REJECTED。
5. **审计字段**：所有业务表均包含 `created_at`、`updated_at`，逻辑删除表额外包含 `is_deleted`。`operation_log` 仅记录操作时间无更新需求，故仅有 `created_at`。
