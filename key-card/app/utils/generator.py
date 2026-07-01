import os
from flask import current_app
from jinja2 import Environment, FileSystemLoader, select_autoescape


def generate_site(cards, template_name):
    templates_dir = current_app.config['OUTPUT_TEMPLATES_DIR']
    template_path = os.path.join(templates_dir, template_name)

    if not os.path.isdir(template_path):
        return '<h1>模板不存在</h1>'

    env = Environment(
        loader=FileSystemLoader(template_path),
        autoescape=select_autoescape(['html', 'xml']),
    )

    template = env.get_template('template.html')
    html = template.render(cards=cards, site_title='关键卡片')
    return html
