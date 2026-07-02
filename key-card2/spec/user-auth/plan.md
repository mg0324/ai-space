# Implementation Plan: 用户注册与登录

**Input**: Feature specification from `spec/user-auth/spec.md`

## Summary

为关键卡片系统添加单管理员认证。使用 Spring Security + JWT 实现后端认证拦截，前端通过路由守卫 + Axios 拦截器实现登录态管理。注册仅允许在系统中无用户时进行，登录后签发 JWT token，前端持久化至 localStorage。

## Technical Context

**Language/Version**: Java 17 / Spring Boot 3.3 / Vue 3  
**Primary Dependencies**: Spring Security 6, JJWT (JWT库), Ant Design Vue 4, Vue Router 4, Axios  
**Storage**: SQLite3 (JPA, 新增 User 实体)  
**Testing**: curl 手动验证  
**Target Platform**: 本地 Web 应用 (localhost)  
**Performance Goals**: 登录响应 < 500ms  
**Constraints**: 离线可用、单用户模式、密码加密存储  
**Scale/Scope**: 1 个管理员账户  

## Project Structure

### Documentation (this feature)

```text
spec/user-auth/
├── spec.md              # 需求规格
├── plan.md              # 本文件
└── tasks.md             # 任务列表 (Phase 3 生成)
```

### Source Code (repository root)

```text
src/main/java/com/keycard/
├── entity/User.java                 # 新增：用户实体
├── repository/UserRepository.java   # 新增：用户仓库
├── service/AuthService.java         # 新增：认证服务（注册/登录/密码加密）
├── controller/AuthController.java   # 新增：认证 API（/api/auth/*）
├── config/SecurityConfig.java       # 新增：Spring Security 配置
├── config/JwtUtil.java              # 新增：JWT 工具类
├── config/JwtFilter.java            # 新增：JWT 认证过滤器
├── dto/LoginRequest.java            # 新增：登录请求 DTO
├── dto/RegisterRequest.java         # 新增：注册请求 DTO
├── dto/AuthResponse.java            # 新增：认证响应 DTO
├── exception/GlobalExceptionHandler.java  # 修改：扩展认证异常处理

frontend/src/
├── api/index.js                      # 修改：添加 auth API
├── router/index.js                   # 修改：添加路由守卫、登录/注册路由
├── views/Login.vue                   # 新增：登录页面
├── views/Register.vue                # 新增：注册页面
├── App.vue                           # 修改：根据登录状态控制菜单显示
```

**Structure Decision**: 沿用现有 src/main/java/com/keycard/ 包结构和 frontend/src/ 前端结构，新增认证相关文件。

## Complexity Tracking

无 Constitution 违规。

## Research & Decisions

**Decision 1: 使用 Spring Security + JWT**
- **Rationale**: 前后端分离架构，JWT 无状态认证更适合；Spring Security 提供过滤器链和端点保护
- **Alternatives considered**: Session 认证（需要服务端状态，不够轻量）、手动拦截器（重复造轮子）

**Decision 2: 使用 BCrypt 加密密码**
- **Rationale**: Spring Security 内置 BCryptPasswordEncoder，安全且简单
- **Alternatives considered**: SHA-256（不加盐不够安全）、MD5（已被破解）

**Decision 3: JWT token 存储于前端 localStorage**
- **Rationale**: 前后端分离标准方案，刷新页面不丢失，满足"关闭浏览器后保持登录"需求
- **Alternatives considered**: Cookie（需额外 CSRF 防护）、SessionStorage（关闭浏览器丢失）

**Decision 4: JJWT 作为 JWT 库**
- **Rationale**: Java 生态最成熟的 JWT 库，与 Spring Boot 集成简单
- **Alternatives considered**: java-jwt (Auth0)（功能相当但社区略小）

**Decision 5: 注册端点 /api/auth/register 无条件开放，但 Service 层检查是否已有用户**
- **Rationale**: 单用户模式，注册接口本身不加密，但 Service 层限制只能注册一次
- **Alternatives considered**: 注册完成后关闭端点（过于复杂，无必要）

## Data Model

### User 实体

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 主键 |
| username | String(50) | UNIQUE, NOT NULL | 用户名，2-20字符 |
| password | String(100) | NOT NULL | BCrypt 加密后的密码 |
| createdAt | LocalDateTime | NOT NULL | 创建时间 |

- 单用户模式：表中最多一条记录
- 密码加密：使用 BCrypt，hash 长度约60字符

### 现有实体变更

Card 和 Tag 实体无需变更（单用户模式无数据隔离需求）。

## Contracts & Interfaces

### 后端 API 端点

| 端点 | 方法 | 认证 | 请求体 | 响应 | 说明 |
|------|------|------|--------|------|------|
| `/api/auth/register` | POST | 无 | `{username, password}` | `{token, username}` | 注册（仅无用户时可用） |
| `/api/auth/login` | POST | 无 | `{username, password}` | `{token, username}` | 登录 |
| `/api/auth/check` | GET | 无 | — | `{hasUser}` | 检查系统是否已有用户 |
| `/api/auth/logout` | POST | JWT | — | `{message}` | 退出（前端清除 token，后端仅确认） |

### 请求/响应 DTO

**RegisterRequest**: username (2-20字符), password (6位以上)  
**LoginRequest**: username, password  
**AuthResponse**: token, username  

### JWT Token 规范

- 签发主题：用户名
- 有效期：7 天
- 前端存储：localStorage key = `token`
- 请求头：`Authorization: Bearer <token>`

### 前端路由变更

| 路由 | 组件 | 认证 |
|------|------|------|
| `/login` | Login.vue | 无 |
| `/register` | Register.vue | 无 |
| 其他路由 | — | 需登录（守卫拦截） |

### 前端 Axios 拦截器

- 请求拦截：从 localStorage 读取 token，添加 `Authorization` 头
- 响应拦截：401 状态码时清除 token 并跳转至登录页
