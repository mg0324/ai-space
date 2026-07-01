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

- 仓库处于初始状态，仅有一个初始提交。
- `key-card/` 是预期的项目根目录——一切工作都在该目录下进行。
- Python 3.6.8，Flask 2.0.3，SQLite，PyInstaller 打包。
- 启动后访问 `http://localhost:5000`，浏览器打开管理界面。
- 应用入口：`key-card/run.py`，Flask app factory 在 `key-card/app/__init__.py`。
- 数据库模型：`key-card/app/models.py`（Card、Tag 多对多关系），SQLite 文件自动创建在 `key-card/instance/` 下。
- 标签管理：`key-card/app/routes/tags.py`，支持标签的创建、重命名、删除（从所有卡片解绑）。
- 输出模板目录：`key-card/output_templates/`，每个子目录为一个模板（含 `template.html` + `meta.json`），热加载无需重启。
- 生成静态网页的预览通过 `/generate/preview` POST 接口返回 HTML 片段，导出到 `key-card/output/` 目录。
- 卡片数据支持 JSON 导入/导出（`/cards/export`、`/cards/import`）。
