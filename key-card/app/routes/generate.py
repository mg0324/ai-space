import os
import json
import shutil
from flask import Blueprint, render_template, request, jsonify, current_app, send_from_directory
from app import db
from app.models import Card
from app.routes.templates import get_available_templates
from app.utils.generator import generate_site

generate_bp = Blueprint('generate', __name__)


@generate_bp.route('/')
def index():
    cards = Card.query.order_by(Card.updated_at.desc()).all()
    templates = get_available_templates()
    all_tags = set()
    for c in cards:
        for t in c.tags.split(',') if c.tags else []:
            t = t.strip()
            if t:
                all_tags.add(t)
    return render_template('generate/index.html', cards=cards,
                           templates=templates, tags=sorted(all_tags))


@generate_bp.route('/preview', methods=['POST'])
def preview():
    card_ids = request.form.getlist('card_ids', type=int)
    template_name = request.form.get('template', '')

    if not card_ids or not template_name:
        return jsonify({'error': '请选择卡片和模板'}), 400

    cards = Card.query.filter(Card.id.in_(card_ids)).all()
    cards.sort(key=lambda c: card_ids.index(c.id))

    output = generate_site([c.to_dict() for c in cards], template_name)

    return jsonify({'html': output})


@generate_bp.route('/export', methods=['POST'])
def export():
    card_ids = request.form.getlist('card_ids', type=int)
    template_name = request.form.get('template', '')

    if not card_ids or not template_name:
        return jsonify({'error': '请选择卡片和模板'}), 400

    cards = Card.query.filter(Card.id.in_(card_ids)).all()
    cards.sort(key=lambda c: card_ids.index(c.id))

    output_dir = current_app.config['OUTPUT_DIR']
    export_name = template_name + '_' + str(len(card_ids)) + 'cards'
    export_path = os.path.join(output_dir, export_name)
    os.makedirs(export_path, exist_ok=True)

    html = generate_site([c.to_dict() for c in cards], template_name)
    with open(os.path.join(export_path, 'index.html'), 'w', encoding='utf-8') as f:
        f.write(html)

    return jsonify({'path': export_path, 'message': '导出成功'})
