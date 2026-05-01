# Java SpringBoot 全栈开发 Skills 介绍

本文档介绍一套专为 Java SpringBoot 全栈开发设计的 agent skills 集合，涵盖从项目创建、API 设计、数据库操作、安全认证、测试、DevOps 到项目管理的完整开发流程。

---

## 一、核心开发技能

### 1.1 `github/awesome-copilot@java-springboot`

**安装量**：13,200+ | **来源**：awesome-copilot

**功能介绍**：
提供 SpringBoot 项目开发的核心规范和最佳实践，涵盖：
- Spring Boot 项目结构设计
- 主流依赖配置（MyBatis、Spring Data JPA、Redis 等）
- RESTful API 设计规范
- 全局异常处理与响应封装
- 统一日志管理

**适用场景**：
项目初始化和核心架构设计阶段。

---

### 1.2 `github/awesome-copilot@create-spring-boot-java-project`

**安装量**：8,600+ | **来源**：awesome-copilot

**功能介绍**：
快速生成 Spring Boot 项目的脚手架代码，包括：
- Maven/Gradle 项目结构
- 标准包结构划分（controller、service、mapper 等）
- 基础配置文件模板
- 常用注解和配置示例

**适用场景**：
新项目创建，快速初始化。

---

### 1.3 `mindrally/skills@java-spring-development`

**安装量**：363 | **来源**：mindrally/skills

**功能介绍**：
Java Spring 生态系统的全面开发指南，涵盖：
- Spring Core 与 Spring Boot 核心概念
- Bean 生命周期与管理
- 依赖注入最佳实践
- AOP 面向切面编程
- 事务管理

**适用场景**：
深入理解 Spring 框架原理，提升代码质量。

---

### 1.4 `mindrally/skills@java`

**安装量**：262 | **来源**：mindrally/skills

**功能介绍**：
Java 基础与高级特性指南，包括：
- Java 基础语法与新特性
- 集合框架源码解析
- 并发编程与 JUC
- JVM 调优基础知识
- 设计模式应用

**适用场景**：
夯实 Java 基础，理解底层原理。

---

### 1.5 `mindrally/skills@spring-boot`

**安装量**：249 | **来源**：mindrally/skills

**功能介绍**：
Spring Boot 专项指南，包含：
- Spring Boot 自动配置原理
- Starter 自定义
- 配置文件加载顺序
- Actuator 监控
- 快速故障排查

**适用场景**：
掌握 Spring Boot 高级特性，优化项目配置。

---

### 1.6 `mindrally/skills@spring-framework`

**安装量**：250 | **来源**：mindrally/skills

**功能介绍**：
Spring Framework 核心模块详解：
- Spring MVC 请求处理流程
- 处理器映射与适配器
- 视图解析机制
- 拦截器与过滤器
- Spring 事件机制

**适用场景**：
深入理解 Web 层原理，排查复杂请求问题。

---

## 二、构建工具技能

### 2.1 `pluginagentmarketplace/custom-plugin-java@java-maven`

**安装量**：493 | **来源**：pluginagentmarketplace/custom-plugin-java

**功能介绍**：
Maven 项目管理与依赖优化指南：
- POM 文件结构解析
- 依赖作用域与传递
- Maven 仓库配置
- 多模块项目构建
- 常用命令与插件使用

**适用场景**：
项目依赖管理，构建优化。

---

### 2.2 `pluginagentmarketplace/custom-plugin-java@java-gradle`

**安装量**：190 | **来源**：pluginagentmarketplace/custom-plugin-java

**功能介绍**：
Gradle 现代化构建指南：
- Groovy/Kotlin DSL 构建脚本
- 依赖管理技巧
- 多项目构建
- 构建缓存与加速
- 与 Spring Boot 集成

**适用场景**：
选择 Gradle 作为构建工具的项目。

---

### 2.3 `pluginagentmarketplace/custom-plugin-java@java-maven-gradle`

**安装量**：144 | **来源**：pluginagentmarketplace/custom-plugin-java

**功能介绍**：
Maven 与 Gradle 对比与迁移指南：
- 两大构建工具对比
- 项目迁移步骤
- 混合使用技巧
- 最佳实践总结

**适用场景**：
构建工具选型或迁移场景。

---

## 三、数据库技能

### 3.1 `personamanagmentlayer/pcl@postgresql-expert`

**安装量**：267 | **来源**：personamanagmentlayer/pcl

**功能介绍**：
PostgreSQL 专家级指南，包含：
- SQL 高级查询
- 索引优化策略
- 数据库设计范式
- 性能调优
- 备份与恢复

**适用场景**：
PostgreSQL 作为生产数据库的项目。

---

### 3.2 `teachingai/full-stack-skills@postgresql`

**安装量**：45 | **来源**：teachingai/full-stack-skills

**功能介绍**：
PostgreSQL 快速上手指南：
- 基础 SQL 操作
- 表设计与关联查询
- 事务与并发控制
- 数据库工具使用

**适用场景**：
团队采用 PostgreSQL 时的快速学习。

---

## 四、API 设计技能

### 4.1 `supercent-io/skills-template@api-documentation`

**安装量**：11,700+ | **来源**：supercent-io/skills-template

**功能介绍**：
API 文档编写规范与工具使用：
- OpenAPI/Swagger 规范
- 接口文档自动生成
- Markdown 文档模板
- API 版本管理
- 文档维护流程

**适用场景**：
项目接口文档建设，提升团队协作效率。

---

### 4.2 `bobmatnyc/claude-mpm-skills@graphql`

**安装量**：149 | **来源**：bobmatnyc/claude-mpm-skills

**功能介绍**：
GraphQL 技术指南：
- GraphQL 核心概念
- Schema 定义
- 查询与变更
- 订阅功能
- 与 Spring Boot 集成

**适用场景**：
需要灵活数据查询的现代 Web 应用。

---

### 4.3 `majiayu000/claude-arsenal@api-design`

**安装量**：61 | **来源**：majiayu000/claude-arsenal

**功能介绍**：
RESTful API 设计最佳实践：
- 资源命名规范
- HTTP 方法正确使用
- 状态码选择
- 分页与过滤
- API 版本策略

**适用场景**：
设计清晰、规范的 REST 接口。

---

## 五、测试技能

### 5.1 `teachingai/full-stack-skills@junit`

**安装量**：78 | **来源**：teachingai/full-stack-skills

**功能介绍**：
JUnit 5 单元测试指南：
- 单元测试基础概念
- JUnit 5 新特性
- 断言与假设
- 参数化测试
- Spring Boot 测试集成

**适用场景**：
为 Java 项目编写单元测试。

---

### 5.2 `claude-dev-suite/claude-dev-suite@junit`

**安装量**：34 | **来源**：claude-dev-suite

**功能介绍**：
JUnit 测试进阶技巧：
- Mock 对象使用
- 集成测试策略
- 测试覆盖率
- 测试报告生成
- 持续集成中的测试

**适用场景**：
提升测试质量，覆盖关键业务逻辑。

---

### 5.3 `partme-ai/full-stack-skills@junit`

**安装量**：24 | **来源**：partme-ai/full-stack-skills

**功能介绍**：
全栈测试实战指南：
- Service 层测试
- Controller 层测试
- 数据库测试
- 端到端测试思路

**适用场景**：
完整的测试体系建设。

---

## 六、安全认证技能

### 6.1 `mindrally/skills@jwt-security`

**安装量**：590 | **来源**：mindrally/skills

**功能介绍**：
JWT 安全认证完整指南：
- JWT 原理与结构
- Token 生成与验证
- 刷新机制
- 权限拦截
- 安全最佳实践

**适用场景**：
实现无状态认证，保护 API 安全。

---

### 6.2 `secondsky/claude-skills@api-authentication`

**安装量**：197 | **来源**：secondsky/claude-skills

**功能介绍**：
API 认证机制详解：
- 多种认证方式对比
- OAuth 2.0 流程
- API Key 使用
- 签名验证
- 跨域安全处理

**适用场景**：
设计安全的 API 认证体系。

---

### 6.3 `secondsky/claude-skills@session-management`

**安装量**：152 | **来源**：secondsky/claude-skills

**功能介绍**：
会话管理指南：
- Session 与 Token 对比
- 分布式 Session
- Session 存储策略
- 安全Cookie配置
- 登录超时处理

**适用场景**：
处理登录状态与用户会话。

---

## 七、DevOps 与容器化技能

### 7.1 `pluginagentmarketplace/custom-plugin-java@java-docker`

**安装量**：135 | **来源**：pluginagentmarketplace/custom-plugin-java

**功能介绍**：
Java 应用 Docker 化指南：
- Dockerfile 编写
- Jib 插件使用
- 镜像优化
- 多阶段构建
- Docker Compose 编排

**适用场景**：
将 Spring Boot 应用容器化部署。

---

### 7.2 `absolutelyskilled/absolutelyskilled@docker-kubernetes`

**安装量**：136 | **来源**：absolutelyskilled

**功能介绍**：
Docker 与 Kubernetes 全指南：
- Docker 基础与进阶
- K8s 核心概念
- Pod、Service、Deployment
- 滚动更新与回滚
- 配置管理

**适用场景**：
容器编排与生产环境部署。

---

### 7.3 `bobmatnyc/claude-mpm-skills@github-actions`

**安装量**：369 | **来源**：bobmatnyc/claude-mpm-skills

**功能介绍**：
GitHub Actions CI/CD 实践：
- Workflow 编写
- Maven/Gradle 构建
- 自动化测试
- 镜像构建与推送
- 部署到云平台

**适用场景**：
搭建自动化构建与发布流程。

---

### 7.4 `ruvnet/ruflo@agent-ops-cicd-github`

**安装量**：157 | **来源**：ruvnet/ruflo

**功能介绍**：
CI/CD 流程设计指南：
- 持续集成策略
- 分支管理流程
- 代码质量门禁
- 自动部署流水线
- 监控与告警

**适用场景**：
构建完整的 DevOps 流程。

---

## 八、微服务与架构技能

### 8.1 `aj-geddes/useful-ai-prompts@microservices-architecture`

**安装量**：307 | **来源**：aj-geddes

**功能介绍**：
微服务架构设计指南：
- 微服务拆分原则
- 服务通信方式
- API Gateway
- 服务注册与发现
- 分布式事务

**适用场景**：
设计或重构为微服务架构。

---

### 8.2 `nickcrew/claude-ctx-plugin@microservices-patterns`

**安装量**：41 | **来源**：nickcrew

**功能介绍**：
微服务设计模式：
- Saga 模式
- CQRS 模式
- 事件溯源
- 服务网格
- 限流与熔断

**适用场景**：
解决微服务中的复杂工程问题。

---

## 九、代码质量技能

### 9.1 `rmyndharis/antigravity-skills@code-refactoring-refactor-clean`

**安装量**：61 | **来源**：rmyndharis/antigravity-skills

**功能介绍**：
代码重构指南：
- 重构时机判断
- 常用重构手法
- 技术债务管理
- 重构安全检查
- 重构与测试结合

**适用场景**：
改善现有代码质量，降低维护成本。

---

### 9.2 `olshansk/agent-skills@cmd-clean-code`

**安装量**：18 | **来源**：olshansk/agent-skills

**功能介绍**：
整洁代码编写规范：
- 命名规范
- 函数设计原则
- 注释与文档
- 代码格式
- SOLID 原则

**适用场景**：
编写可读、可维护的高质量代码。

---

## 十、项目管理技能

### 10.1 `404kidwiz/claude-supercode-skills@project-manager`

**安装量**：987 | **来源**：404kidwiz

**功能介绍**：
AI 辅助项目管理指南：
- 任务分解与估算
- 进度跟踪
- 风险识别
- 团队协作
- 与 AI 协作提升效率

**适用场景**：
提升个人或团队的项目管理能力。

---

### 10.2 `jezweb/claude-skills@project-session-management`

**安装量**：383 | **来源**：jezweb

**功能介绍**：
项目会话管理最佳实践：
- 会话记录与回顾
- 上下文管理
- 状态同步
- 知识积累

**适用场景**：
长期项目中的知识管理与连续性保障。

---

### 10.3 `miles990/claude-software-skills@project-management`

**安装量**：200 | **来源**：miles990

**功能介绍**：
软件项目管理系统指南：
- 敏捷开发流程
- 迭代计划
- 成果物管理
- 团队沟通
- 质量控制

**适用场景**：
采用敏捷方法的软件开发团队。

---

## 十一、技能安装建议

### 必装技能（强烈推荐）

| Skill | 安装命令 |
|-------|----------|
| `github/awesome-copilot@java-springboot` | `npx skills add github/awesome-copilot@java-springboot -g -y` |
| `mindrally/skills@jwt-security` | `npx skills add mindrally/skills@jwt-security -g -y` |
| `supercent-io/skills-template@api-documentation` | `npx skills add supercent-io/skills-template@api-documentation -g -y` |

### 推荐安装

| Skill | 安装命令 |
|-------|----------|
| `pluginagentmarketplace/custom-plugin-java@java-maven` | `npx skills add pluginagentmarketplace/custom-plugin-java@java-maven -g -y` |
| `bobmatnyc/claude-mpm-skills@github-actions` | `npx skills add bobmatnyc/claude-mpm-skills@github-actions -g -y` |
| `teachingai/full-stack-skills@junit` | `npx skills add teachingai/full-stack-skills@junit -g -y` |
| `pluginagentmarketplace/custom-plugin-java@java-docker` | `npx skills add pluginagentmarketplace/custom-plugin-java@java-docker -g -y` |

### 按需安装

根据项目实际需求选择：
- PostgreSQL 项目 → `personamanagmentlayer/pcl@postgresql-expert`
- 微服务架构 → `aj-geddes/useful-ai-prompts@microservices-architecture`
- 代码重构 → `rmyndharis/antigravity-skills@code-refactoring-refactor-clean`
- 项目管理 → `404kidwiz/claude-supercode-skills@project-manager`

---

## 十二、快速开始

一次性安装所有推荐技能：

```bash
npx skills add github/awesome-copilot@java-springboot -g -y
npx skills add mindrally/skills@jwt-security -g -y
npx skills add supercent-io/skills-template@api-documentation -g -y
npx skills add pluginagentmarketplace/custom-plugin-java@java-maven -g -y
npx skills add bobmatnyc/claude-mpm-skills@github-actions -g -y
npx skills add teachingai/full-stack-skills@junit -g -y
npx skills add pluginagentmarketplace/custom-plugin-java@java-docker -g -y
npx skills add 404kidwiz/claude-supercode-skills@project-manager -g -y
```

---

> 更多技能请访问 [https://skills.sh/](https://skills.sh/)