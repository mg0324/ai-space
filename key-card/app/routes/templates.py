import os
import json
from flask import Blueprint, render_template, current_app
from jinja2 import Environment, FileSystemLoader, select_autoescape

templates_bp = Blueprint('templates', __name__)


def get_available_templates():
    templates_dir = current_app.config['OUTPUT_TEMPLATES_DIR']
    result = []
    if not os.path.isdir(templates_dir):
        return result

    for name in sorted(os.listdir(templates_dir)):
        dir_path = os.path.join(templates_dir, name)
        if os.path.isdir(dir_path):
            meta_path = os.path.join(dir_path, 'meta.json')
            meta = {}
            if os.path.isfile(meta_path):
                with open(meta_path, 'r', encoding='utf-8') as f:
                    meta = json.load(f)
            result.append({
                'name': name,
                'label': meta.get('label', name),
                'description': meta.get('description', ''),
                'preview': meta.get('preview', ''),
            })
    return result


@templates_bp.route('/')
def list():
    templates = get_available_templates()
    return render_template('templates/list.html', templates=templates)
