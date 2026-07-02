# ai-space

## 语言

- 所有助手应答必须使用中文

## 仓库结构

- `key-card2/` — 活跃项目，关键卡片 v2（静态网页生成器）
- `key-card/` — 旧版 Python/Flask 实现，不再维护，仅作功能参考

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
- Vite 代理 `/api` 请求到后端 8080，无需 CORS
- `npm run build` 产物直接输出到 Spring Boot 静态资源目录，生产模式前后端一体

## key-card2 技术栈

- 后端：Java 17 + Spring Boot 3.3 + Spring Data JPA + SQLite3（hibernate-community-dialects）
- 前端：Vue 3 + Vue Router + Ant Design Vue 4 + Axios + Vite 6
- 数据库：SQLite，文件位于项目根目录 `data.db`，JPA ddl-auto=update 自动建表

## 后端结构

- `src/main/java/com/keycard/`
  - `entity/` — Card（多对多 Tag）、Tag
  - `dto/` — CardDTO/CardRequest、TagDTO/TagRequest、GenerateRequest、TemplateDTO
  - `repository/` — CardRepository、TagRepository（Spring Data JPA）
  - `service/` — CardService、TagService、TemplateService、GenerateService
  - `controller/` — CardController `/api/cards`、TagController `/api/tags`、TemplateController `/api/templates`、GenerateController `/api/generate`
  - `config/` — WebConfig（CORS）
  - `exception/` — GlobalExceptionHandler

## API 端点

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/cards` | GET | 列表（?tag=&q= 搜索筛选）|
| `/api/cards` | POST | 创建 |
| `/api/cards/{id}` | GET/PUT/DELETE | 读写删 |
| `/api/cards/export` | GET | 导出 JSON |
| `/api/cards/import` | POST | 导入 JSON 数组 |
| `/api/tags` | GET/POST | 列表/创建 |
| `/api/tags/{id}` | PUT/DELETE | 重命名/删除 |
| `/api/templates` | GET | 列出可用模板 |
| `/api/generate/preview` | POST | 预览 {cardIds, template} |
| `/api/generate/export` | POST | 导出 {cardIds, template} |

## 输出模板

- 位于 `templates/output/`，每个模板一个目录
- 包含 `meta.json`（label, description）和 `template.html`
- 内置三种：note（笔记）、timeline（时间线）、comparison（网格对照）
- 模板使用 `{{ site_title }}` 和 `{{ cards_html }}` 占位符

## 注意

- 当前 Maven 默认使用 Java 17（系统 JAVA_HOME），需求文档要求 Java 21，pom.xml 暂设为 17
- 导出目录 `output/` 和数据库 `data.db` 运行时自动创建，已在 .gitignore 中
