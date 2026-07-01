# ai-space

## 结构

- `key-card/` — 唯一的项目目录，关键卡片桌面工具

## 命令

```bash
# 启动开发服务器（key-card/ 目录下）
python run.py

# 安装依赖
pip install -r requirements.txt
```

## 语言

- 所有助手应答必须使用中文。

## 备注

- `key-card/` 是预期的项目根目录——一切工作都在该目录下进行。
- Python 3.6.8，Flask 2.0.3，SQLite，PyInstaller 打包。
- 启动后访问 `http://localhost:5000`，浏览器打开管理界面。
- 应用入口：`key-card/run.py`，Flask app factory 在 `key-card/app/__init__.py`。
- 前端架构：Vue 3 + Element Plus + Vue Router 4 SPA，所有库使用 UMD/CDN 静态文件（无需构建步骤）。
- 后端 API：`key-card/app/routes/api.py`，RESTful JSON API（`/api/cards`、`/api/tags`、`/api/templates`、`/api/generate/preview`、`/api/generate/export`）。
- SPA 入口：`key-card/app/routes/main.py` 渲染 `key-card/app/templates/index.html`。
- 前端 JS：`key-card/app/static/js/app.js`，含所有 Vue 组件。
- Vue 模板用 `{% raw %}` 包裹以避免 Jinja2 的 `{{ }}` 语法冲突。
- 数据库模型：`key-card/app/models.py`（Card、Tag 多对多关系），SQLite 文件自动创建在 `key-card/instance/` 下。
- 输出模板目录：`key-card/output_templates/`，每个子目录为一个模板（含 `template.html` + `meta.json`），热加载无需重启。
- 生成静态网页的预览通过 `/api/generate/preview` POST 接口返回 HTML 片段，导出到 `key-card/output/` 目录。
- 卡片数据支持 JSON 导入/导出（`/cards/export`、`/cards/import`）。
