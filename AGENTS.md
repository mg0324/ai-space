# ai-space

## 语言

- 所有助手应答必须使用中文

## 仓库结构

- `key-card2/` — 唯一活跃项目，关键卡片 v2（静态网页生成器）
- `key-card2/spec/` — 功能规格与开发计划（如 `user-auth/`）
- `key-card2/docs/requirements.md` — 原始需求文档

## key-card2 开发命令

```bash
# 后端（在 key-card2/ 目录下）
mvn compile                  # 编译后端
mvn spring-boot:run          # 启动后端 (localhost:8080)

# 前端（在 key-card2/frontend/ 目录下）
npm run dev                  # 启动 Vite 开发服务器 (localhost:5173，自动代理 /api → 8080)
npm run build                # 构建前端到 src/main/resources/static/
```

- 开发时前端 `npm run dev` + 后端 `mvn spring-boot:run` 分开运行
- Vite 代理 `/api` 请求到后端 8080，开发环境无需额外 CORS 配置
- `npm run build` 产物直接输出到 Spring Boot 静态资源目录，生产模式前后端一体
- Spring DevTools + Vite HMR 支持热加载（后端改 Java、前端改 Vue 均自动刷新）
- **无测试套件**：项目当前没有单元测试或集成测试，改代码后需手动验证

## key-card2 技术栈

- 后端：Java 17 + Spring Boot 3.3 + Spring Data JPA + SQLite3（hibernate-community-dialects）
- 前端：Vue 3 + Vue Router + Ant Design Vue 4 + Axios + Vite 6
- 数据库：SQLite，文件位于 key-card2 根目录 `data.db`，JPA ddl-auto=update 自动建表
- 认证：Spring Security + JWT（jjwt 0.12.6），单用户模式，BCrypt 密码加密

## 后端结构

`src/main/java/com/keycard/`

| 目录 | 关键类 | 说明 |
|------|--------|------|
| `entity/` | Card、Tag、User | JPA 实体，Card-Tag 多对多 |
| `dto/` | CardDTO/CardRequest、TagDTO/TagRequest、GenerateRequest、TemplateDTO、LoginRequest/RegisterRequest/AuthResponse | 请求/响应 DTO |
| `repository/` | CardRepository、TagRepository、UserRepository | Spring Data JPA |
| `service/` | CardService、TagService、TemplateService、GenerateService、AuthService | 业务逻辑 |
| `controller/` | CardController、TagController、TemplateController、GenerateController、AuthController | REST API |
| `config/` | SecurityConfig、JwtFilter、JwtUtil、WebConfig | 安全与 CORS 配置 |
| `exception/` | GlobalExceptionHandler | 统一异常处理 |

## API 端点

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 注册（公开） |
| `/api/auth/login` | POST | 登录（公开） |
| `/api/cards` | GET | 列表（?tag=&q= 搜索筛选，需 JWT）|
| `/api/cards` | POST | 创建 |
| `/api/cards/{id}` | GET/PUT/DELETE | 读写删 |
| `/api/cards/export` | GET | 导出 JSON |
| `/api/cards/import` | POST | 导入 JSON 数组 |
| `/api/tags` | GET/POST | 列表/创建 |
| `/api/tags/{id}` | PUT/DELETE | 重命名/删除 |
| `/api/templates` | GET | 列出可用模板 |
| `/api/generate/preview` | POST | 预览 {cardIds, template} |
| `/api/generate/export` | POST | 导出 {cardIds, template} |

- `/api/auth/**` 公开，其他 `/api/**` 需要 JWT Bearer token
- 静态资源（HTML/CSS/JS）无需认证

## 输出模板

- 位于 `templates/output/`，每个模板一个目录
- 包含 `meta.json`（label, description）和 `template.html`
- 内置七种：note（笔记）、timeline（时间线）、comparison（网格对照）、faq（问答）、kanban（看板）、gallery（画廊）、mindmap（思维导图）
- 模板使用 `{{ site_title }}` 和 `{{ cards_html }}` 占位符

## 注意

- Maven 默认使用 Java 17（系统 JAVA_HOME），需求文档要求 Java 21，pom.xml 暂设为 17
- 导出目录 `output/` 和数据库 `data.db` 运行时自动创建，已在 .gitignore 中
- 前端无 lint/format 工具配置，代码风格靠人工保持
