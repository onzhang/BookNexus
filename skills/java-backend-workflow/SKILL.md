---
name: java-backend-workflow
description: |
  Standard workflow for Java backend development, covering the full lifecycle:
  requirements analysis → database design → API design → layered coding → 
  testing → deployment. MySQL-specific database best practices included.
  Framework-agnostic — applicable to Spring Boot, Quarkus, Micronaut, etc.
license: MIT
---

# Java Backend Development Workflow

> A comprehensive, framework-agnostic guide for Java backend development using MySQL as the database.

---

## 1. Requirements Analysis

### 1.1 Functional Requirements
- Extract core business modules from product documents / user stories.
- Identify actors and roles (admin, regular user, third-party system).
- List all user-facing and internal features.

### 1.2 Non-Functional Requirements
- **Performance**: Expected QPS, response time SLOs, concurrency targets.
- **Security**: Authentication mechanism, data encryption, access control model.
- **Availability**: Uptime targets, failover strategy, disaster recovery.
- **Compliance**: GDPR, PCI-DSS, or other applicable regulatory constraints.

### 1.3 Entity Identification
- Extract nouns from user stories as candidate entities (e.g., User, Order, Product).
- Define entity relationships (one-to-one, one-to-many, many-to-many).

### 1.4 Deliverables
- [ ] Functional requirement checklist
- [ ] Actor-role matrix
- [ ] Core entity list with brief descriptions
- [ ] Non-functional requirement specification

---

## 2. Database Design (MySQL)

### 2.1 ER Diagram & Normalization
1. Draw an Entity-Relationship (ER) diagram covering all core entities.
2. Apply **3rd Normal Form (3NF)** by default; denormalize only for proven performance bottlenecks.
3. Resolve M:N relationships with junction tables (e.g., `user_role`).

### 2.2 MySQL Table Design Best Practices

#### Naming Conventions
| Item        | Convention              | Example               |
|-------------|-------------------------|-----------------------|
| Table name  | lowercase + underscores | `borrow_record`       |
| Column name | lowercase + underscores | `created_at`          |
| Primary key | `id`                    | `id BIGINT UNSIGNED`  |
| Index       | `idx_<table>_<columns>` | `idx_user_email`      |
| Unique key  | `uk_<table>_<columns>`  | `uk_book_isbn`        |
| Foreign key | `fk_<table>_<ref>`      | `fk_record_user_id`   |

#### Mandatory Audit Columns
Every table should include these columns (or use a shared base table / ORM hooks):
- `id` — `BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY`
- `created_at` — `DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `updated_at` — `DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- `deleted_at` — `DATETIME NULL DEFAULT NULL` (for soft-delete)
- `version` — `INT NOT NULL DEFAULT 0` (for optimistic locking)

#### Data Type Selection
| Data Type      | Use Case                                      |
|----------------|-----------------------------------------------|
| `BIGINT`       | Primary keys, foreign keys                    |
| `VARCHAR(N)`   | Variable-length strings (names, titles)       |
| `TEXT`         | Long-form content (articles, descriptions)    |
| `DECIMAL(M,D)` | Monetary values (never use FLOAT for money)   |
| `TINYINT`      | Boolean flags, status codes with <= 255 values|
| `DATETIME`     | Timestamps (always store in UTC)              |
| `JSON`         | Semi-structured data (MySQL 5.7+)             |

#### Indexing Strategy
1. **Every foreign key column** must be indexed.
2. Create **composite indexes** for frequently combined WHERE conditions; follow the leftmost prefix rule.
3. Use **covering indexes** to avoid table lookups for hot queries.
4. Avoid over-indexing: each index slows down INSERT/UPDATE/DELETE.
5. Use `EXPLAIN` to verify index usage for all critical queries.
6. Plan for slow-query logging (`long_query_time`, `slow_query_log_file`).

```sql
-- Example: Composite index for the query pattern
-- SELECT * FROM orders WHERE user_id = ? AND status = ? ORDER BY created_at DESC;
CREATE INDEX idx_orders_user_status_created ON orders(user_id, status, created_at);
```

#### Table Engine & Charset
- Use `InnoDB` for transaction support and row-level locking.
- Default charset: `utf8mb4` with collation `utf8mb4_unicode_ci` (supports full Unicode including emoji).

#### Connection Pool Configuration
- Minimum idle connections: `10`
- Maximum active connections: `20–50` (tune based on `max_connections` and app instances)
- Connection timeout: `30s`
- Always enable `testOnBorrow` / `testWhileIdle` to detect broken connections

### 2.3 Database Versioning
- Use a migration tool: **Flyway** (SQL-based) or **Liquibase** (XML/YAML/JSON-based).
- Never modify an already-applied migration script; always create a new version.
- Keep seed/test data in separate scripts.

### 2.4 SQL Writing Guidelines
- Never use `SELECT *` in production code.
- Use parameterized queries or prepared statements — never concatenate user input into SQL.
- Use `LIMIT` for all unbounded queries.
- Avoid correlated subqueries on large tables; prefer JOINs.

---

## 3. RESTful API Design

### 3.1 URL Conventions
```
GET    /api/v1/{resources}           # List (paginated)
GET    /api/v1/{resources}/{id}      # Get by ID
POST   /api/v1/{resources}           # Create
PUT    /api/v1/{resources}/{id}      # Full replace
PATCH  /api/v1/{resources}/{id}      # Partial update
DELETE /api/v1/{resources}/{id}      # Delete (prefer soft-delete)
```

- Use plural nouns, lowercase, hyphen-separated (e.g., `/api/v1/order-items`).
- Version in the URL path (`/v1/`, `/v2/`).
- Limit resource nesting to **2 levels** max; deeper queries should use flat endpoints with query parameters.

### 3.2 HTTP Status Codes
| Code  | Meaning                | Typical Scenario                        |
|-------|------------------------|-----------------------------------------|
| 200   | OK                     | Successful GET, PUT, PATCH, DELETE     |
| 201   | Created                | Successful POST                        |
| 204   | No Content             | Successful DELETE with no body         |
| 400   | Bad Request            | Malformed input / validation failure   |
| 401   | Unauthorized           | Missing or invalid credentials         |
| 403   | Forbidden              | Insufficient permissions               |
| 404   | Not Found              | Resource does not exist                |
| 409   | Conflict               | Business rule violation (e.g., duplicate) |
| 422   | Unprocessable Entity   | Semantic validation failure            |
| 429   | Too Many Requests      | Rate limit exceeded                    |
| 500   | Internal Server Error  | Unhandled server exception             |

### 3.3 Unified Response Envelope
```json
{
  "code": 200,
  "message": "OK",
  "data": { ... },
  "timestamp": 1714387200000
}
```
- Use a global `ResponseBodyAdvice` or filter to wrap all responses uniformly.
- Return business error codes in the `code` field for the frontend to map user-facing messages.

### 3.4 Pagination
```json
GET /api/v1/books?page=1&size=20&sort=createdAt,desc

{
  "code": 200,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "totalElements": 150,
    "totalPages": 8,
    "page": 1,
    "size": 20,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1714387200000
}
```
- Default `page=1`, `size=20`, max `size=100`.
- Page numbering is 1-based for consumer convenience.

### 3.5 API Documentation
- Use **SpringDoc OpenAPI** (or **Swagger** / **Knife4j**) annotations on controllers.
- Expose Swagger UI at `/swagger-ui.html` and OpenAPI JSON at `/v3/api-docs`.

### 3.6 Request Validation
- Use Bean Validation annotations (`@NotNull`, `@Size`, `@Email`, `@Pattern`).
- Return 400 / 422 with field-level error details on validation failure.
- Always validate on the server side — never trust client-side validation alone.

---

## 4. Layered Architecture

### 4.1 Standard Layers
```
controller/      → HTTP handling, parameter binding, response wrapping
service/         → Business logic, transaction orchestration
repository/dao/  → Data access interface
entity/domain/   → Database entities, domain models
dto/             → Data Transfer Objects (request / response)
config/          → Framework configuration beans
common/          → Utilities, constants, enums, custom exceptions
```

### 4.2 Layer Rules
- **Controller**: Must NOT contain business logic. Delegate to Service immediately after input validation.
- **Service**: Houses all business rules. One public method = one business use case. Use `@Transactional` only where atomicity is required.
- **Repository/DAO**: Pure data access — no business logic, no HTTP context references.
- **Entity vs DTO**: Entities map to database tables. DTOs are for I/O at controller boundaries. Always convert Entity ↔ DTO in the Service layer.

### 4.3 Dependency Rule
```
Controller → Service (interface) → ServiceImpl → Repository/DAO
```
- Dependencies point inward. Inner layers know nothing about outer layers.
- Controller depends on Service **interface** (not implementation), enabling unit testing with mocks.

---

## 5. Exception Handling & Logging

### 5.1 Exception Hierarchy
```
RuntimeException
├── BusinessException          (recoverable / expected)
│   ├── ResourceNotFoundException
│   ├── DuplicateResourceException
│   └── InsufficientPermissionException
└── SystemException            (unexpected / infrastructure)
    ├── DatabaseException
    └── ExternalServiceException
```

### 5.2 Global Exception Handler
Use a centralized handler (e.g., `@ControllerAdvice` / `@RestControllerAdvice`) that:
1. Maps each custom exception to an appropriate HTTP status code and error code.
2. Logs the full stack trace for `SystemException`, only the message for `BusinessException`.
3. Returns the unified response envelope.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(ResourceNotFoundException ex) {
        return Result.error(404, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.error(500, "Internal server error");
    }
}
```

### 5.3 Logging Standards
- Use **SLF4J** as the logging facade (Logback / Log4j2 as implementation).
- **Log Levels**:
  - `ERROR`: System failures, unhandled exceptions (always log stack trace).
  - `WARN`: Degraded operations, retry attempts, deprecated usage.
  - `INFO`: Key business events (login, order placed, payment completed).
  - `DEBUG`: Method entry/exit, SQL parameters, detailed flow data.
  - `TRACE`: Very fine-grained debugging.
- Never log passwords, tokens, credit card numbers, or PII.
- Use MDC (Mapped Diagnostic Context) to propagate `traceId`/`userId` across threads.

### 5.4 Log Format (JSON recommended for production)
```json
{
  "timestamp": "2024-04-29T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.example.service.OrderService",
  "thread": "http-nio-8080-exec-5",
  "traceId": "a1b2c3d4",
  "userId": "1001",
  "message": "Order placed successfully",
  "orderId": "ORD-2024-001"
}
```

---

## 6. Testing

### 6.1 Unit Testing
- Framework: **JUnit 5** (or **TestNG**) + **Mockito** for mocking.
- Target: Service layer business logic (mocking all dependencies).
- Aim for ≥ 70% line coverage on business logic.
- Name convention: `{MethodName}_{Scenario}_{ExpectedResult}` (e.g., `createOrder_insufficientStock_throwsException`).

```java
@Test
void createOrder_insufficientStock_throwsBusinessException() {
    when(inventoryRepo.getStock(anyLong())).thenReturn(0);
    assertThrows(BusinessException.class, () -> orderService.createOrder(request));
}
```

### 6.2 Repository / DAO Testing
- Use `@DataJpaTest` / `@MybatisTest` (or a dedicated test database / Testcontainers).
- Verify SQL correctness on a real MySQL instance. H2 in MySQL-compatibility mode is acceptable for fast local runs but must be verified against real MySQL in CI.

### 6.3 Integration Testing
- Start the full application context with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- Use **Testcontainers** to spin up a real MySQL instance for deterministic test results.
- Test the full flow: HTTP request → Controller → Service → DB → response.
- Use **REST Assured** or **MockMvc** for HTTP assertion.

### 6.4 API / Contract Testing
- Verify all endpoints with Postman collections / Bruno / HTTP files.
- Consider contract testing (**Spring Cloud Contract**, **Pact**) for microservice boundaries.

### 6.5 Performance Testing (Optional)
- Use **JMeter**, **Gatling**, or **k6** for load testing.
- Establish baseline: < 200ms p95 for simple GET by ID; < 500ms p95 for paginated list.

---

## 7. Packaging & Deployment

### 7.1 Build & Package
- **Maven** (`pom.xml`) or **Gradle** (`build.gradle`) for dependency management and build.
- Produce a single executable JAR (Fat JAR / Uber JAR) via `spring-boot-maven-plugin` or `shadow` plugin.
- Separate profiles for `dev`, `test`, `prod` with environment-specific configurations.

```bash
# Maven
mvn clean package -P prod
java -jar target/app.jar --spring.profiles.active=prod

# Gradle
gradle clean build -Pprofile=prod
java -jar build/libs/app.jar
```

### 7.2 Containerization (Docker)
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- Use multi-stage builds to keep images small.
- Never hardcode secrets in the image — use environment variables or a secret manager.

### 7.3 Health Checks & Monitoring
- Expose health/liveness/readiness probes (`/actuator/health`).
- Integrate with **Prometheus** + **Grafana** for metrics visualization.
- Enable **Micrometer** (or custom metrics) for JVM metrics (heap, threads, GC).

### 7.4 CI/CD Pipeline Checklist
- [ ] Code compiles and passes all tests on every push.
- [ ] Static analysis (Checkstyle / PMD / SonarQube) runs.
- [ ] Security scan (dependency vulnerability check, SAST).
- [ ] Docker image built and pushed to registry.
- [ ] Automated deployment to dev/staging environment.
- [ ] Smoke tests run against deployed environment.
- [ ] Production deployment requires manual approval.

---

## 8. Security Best Practices

### 8.1 Authentication & Authorization
- Use **JWT** (stateless) or **OAuth 2.0** / **OIDC** for authentication.
- Implement **RBAC** (Role-Based Access Control) or **ABAC** (Attribute-Based) for authorization.
- Separate public endpoints, authenticated endpoints, and admin endpoints clearly.
- Always validate tokens on every request — never trust a token simply because it's well-formed.

### 8.2 Common Vulnerabilities (OWASP Top 10)
- **SQL Injection**: Use parameterized queries / ORM — never string-concatenate SQL.
- **XSS**: Sanitize user input; set `Content-Security-Policy` headers.
- **CSRF**: Use anti-CSRF tokens for cookie-based sessions; stateless JWT is inherently CSRF-safe.
- **Sensitive Data Exposure**: Encrypt secrets at rest, use HTTPS everywhere.

### 8.3 Dependency Management
- Regularly scan for CVEs using **OWASP Dependency-Check**, **Snyk**, or **Dependabot**.
- Keep dependencies up to date; have a policy for emergency patching of critical vulnerabilities.

---

## 9. Development Checklist

### Pre-Development
- [ ] Requirements documented and signed off
- [ ] ER diagram completed and reviewed
- [ ] API contract (OpenAPI spec) drafted
- [ ] Database migration scripts ready
- [ ] Project skeleton generated (build tool, package structure)

### During Development
- [ ] All foreign keys indexed
- [ ] `EXPLAIN` verified for all dynamic queries
- [ ] Pagination implemented for all list endpoints
- [ ] Global exception handler configured
- [ ] Input validation annotations on all request DTOs
- [ ] Logging messages include trace identifiers
- [ ] Business operations annotated with `@Transactional` where needed
- [ ] Swagger/OpenAPI annotations on all controllers

### Pre-Release
- [ ] Unit test coverage ≥ 70% on business logic
- [ ] All integration tests pass against real MySQL
- [ ] No critical/blocker issues in static analysis
- [ ] Dependency vulnerability scan shows zero critical CVEs
- [ ] Environment-specific configurations verified
- [ ] Health check endpoint responds correctly
- [ ] API documentation is up to date
- [ ] Release notes written

### Post-Release
- [ ] Monitor error logs for the first 24 hours
- [ ] Verify key metrics (latency, error rate) are within SLO
- [ ] Tag the release in version control
- [ ] Archive or cleanup feature flags if applicable

---

## 10. ORM & Technology Selection Guide

### ORM / Data Access
| Technology           | Style                          | Best For                              |
|----------------------|--------------------------------|---------------------------------------|
| **MyBatis**          | SQL mapping (semi-ORM)         | Complex queries, strict SQL control   |
| **MyBatis-Plus**     | MyBatis + ActiveRecord helpers | Rapid CRUD, minimal boilerplate       |
| **JPA / Hibernate**  | Full ORM (JPA specification)   | Standard CRUD, auto DDL, caching      |
| **Spring Data JDBC** | Lightweight DDD-style mapping  | Simpler mapping, no lazy loading      |
| **JOOQ**             | Type-safe SQL DSL              | Compile-time SQL verification         |

### Web Framework
| Technology          | Notes                                      |
|---------------------|--------------------------------------------|
| **Spring Boot**     | De facto standard, most mature ecosystem   |
| **Quarkus**         | GraalVM-native, fast startup, Kubernetes   |
| **Micronaut**       | AOT compilation, fast startup, reactive    |
| **Jakarta EE**      | Standard-based, full application server    |

### Security
| Technology              | Notes                               |
|-------------------------|-------------------------------------|
| **Spring Security**     | Comprehensive, highly customizable  |
| **Apache Shiro**        | Lightweight, simpler API            |
| **JWT (jjwt / nimbus)** | Token handling library              |
| **OAuth 2.0 / OIDC**    | Standardized delegated auth         |

### Caching
| Technology    | Notes                                 |
|---------------|---------------------------------------|
| **Redis**     | Distributed cache, pub/sub, rate limit|
| **Caffeine**  | High-performance local cache          |
| **Ehcache**   | Local/terracotta-backed cache         |

### Message Queue
| Technology     | Notes                                   |
|----------------|-----------------------------------------|
| **RabbitMQ**   | AMQP protocol, flexible routing         |
| **Apache Kafka** | Distributed log, event streaming      |
| **RocketMQ**   | Alibaba, transactional messages         |

---

## 11. Common Commands Reference

```bash
# --- Maven ---
mvn clean compile              # Compile
mvn clean test                 # Run unit tests
mvn clean package -DskipTests  # Package (skip tests)
mvn spring-boot:run            # Run development server
mvn versions:display-dependency-updates  # Check dependency updates

# --- Gradle ---
gradle clean build             # Clean & build
gradle test                    # Run tests
gradle bootRun                 # Run development server

# --- Docker ---
docker build -t my-app:latest .
docker run -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=prod" my-app:latest

# --- MySQL ---
mysql -u root -p -e "CREATE DATABASE myapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p myapp < migration/V1__init.sql

# --- Git ---
git flow init                  # Initialize Git Flow branching
git tag -a v1.0.0 -m "Release v1.0.0"
```
