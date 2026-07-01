import json
import os
from datetime import datetime
from flask import Blueprint, request, jsonify, current_app
from app import db
from app.models import Card, Tag
from app.routes.templates import get_available_templates
from app.utils.generator import generate_site

api_bp = Blueprint('api', __name__, url_prefix='/api')


# ===== Cards =====

@api_bp.route('/cards', methods=['GET'])
def list_cards():
    tag_name = request.args.get('tag')
    search = request.args.get('q')
    query = Card.query.order_by(Card.updated_at.desc())

    if tag_name:
        query = query.filter(Card.tag_list.any(Tag.name == tag_name))
    if search:
        query = query.filter(
            Card.title.contains(search) | Card.content.contains(search)
        )

    cards = query.all()
    return jsonify([c.to_dict() for c in cards])


@api_bp.route('/cards', methods=['POST'])
def create_card():
    data = request.get_json()
    if not data:
        return jsonify({'error': '无效的请求数据'}), 400

    title = (data.get('title') or '').strip()
    content = (data.get('content') or '').strip()
    source = (data.get('source') or '').strip()
    tag_ids = data.get('tag_ids', [])

    if not title or not content:
        return jsonify({'error': '标题和内容不能为空'}), 400

    card = Card(title=title, content=content, source=source)
    if tag_ids:
        card.tag_list = Tag.query.filter(Tag.id.in_(tag_ids)).all()

    db.session.add(card)
    db.session.commit()
    return jsonify(card.to_dict()), 201


@api_bp.route('/cards/<int:card_id>', methods=['GET'])
def get_card(card_id):
    card = Card.query.get_or_404(card_id)
    return jsonify(card.to_dict())


@api_bp.route('/cards/<int:card_id>', methods=['PUT'])
def update_card(card_id):
    card = Card.query.get_or_404(card_id)
    data = request.get_json()
    if not data:
        return jsonify({'error': '无效的请求数据'}), 400

    title = (data.get('title') or '').strip()
    content = (data.get('content') or '').strip()
    if not title or not content:
        return jsonify({'error': '标题和内容不能为空'}), 400

    card.title = title
    card.content = content
    card.source = (data.get('source') or '').strip()
    card.updated_at = datetime.utcnow()

    tag_ids = data.get('tag_ids', [])
    if tag_ids:
        card.tag_list = Tag.query.filter(Tag.id.in_(tag_ids)).all()
    else:
        card.tag_list = []

    db.session.commit()
    return jsonify(card.to_dict())


@api_bp.route('/cards/<int:card_id>', methods=['DELETE'])
def delete_card(card_id):
    card = Card.query.get_or_404(card_id)
    db.session.delete(card)
    db.session.commit()
    return jsonify({'message': '删除成功'})


@api_bp.route('/cards/export', methods=['GET'])
def export_cards():
    cards = Card.query.all()
    return jsonify([c.to_dict() for c in cards])


@api_bp.route('/cards/import', methods=['POST'])
def import_cards():
    data = request.get_json()
    if not data or not isinstance(data, list):
        return jsonify({'error': '请上传 JSON 数组'}), 400

    count = 0
    for item in data:
        if not item.get('title') or not item.get('content'):
            continue
        card = Card(
            title=item['title'],
            content=item['content'],
            source=item.get('source', ''),
        )
        tag_names = item.get('tags', [])
        if isinstance(tag_names, str):
            tag_names = [t.strip() for t in tag_names.split(',') if t.strip()]
        if tag_names:
            tags = []
            for name in tag_names:
                tag = Tag.query.filter(Tag.name == name).first()
                if not tag:
                    tag = Tag(name=name)
                    db.session.add(tag)
                tags.append(tag)
            card.tag_list = tags

        db.session.add(card)
        count += 1

    db.session.commit()
    return jsonify({'message': '导入成功', 'count': count})


# ===== Tags =====

@api_bp.route('/tags', methods=['GET'])
def list_tags():
    tag_list = Tag.query.order_by(Tag.name).all()
    data = [{'id': t.id, 'name': t.name, 'count': len(t.cards)} for t in tag_list]
    return jsonify(data)


@api_bp.route('/tags', methods=['POST'])
def create_tag():
    data = request.get_json()
    name = (data.get('name') or '').strip() if data else ''

    if not name:
        return jsonify({'error': '标签名不能为空'}), 400

    existing = Tag.query.filter(Tag.name == name).first()
    if existing:
        return jsonify({'error': '标签已存在'}), 400

    tag = Tag(name=name)
    db.session.add(tag)
    db.session.commit()
    return jsonify({'id': tag.id, 'name': tag.name}), 201


@api_bp.route('/tags/<int:tag_id>', methods=['PUT'])
def rename_tag(tag_id):
    tag = Tag.query.get_or_404(tag_id)
    data = request.get_json()
    new_name = (data.get('name') or '').strip() if data else ''

    if not new_name:
        return jsonify({'error': '标签名不能为空'}), 400

    existing = Tag.query.filter(Tag.name == new_name, Tag.id != tag_id).first()
    if existing:
        return jsonify({'error': '标签名已存在'}), 400

    tag.name = new_name
    db.session.commit()
    return jsonify({'id': tag.id, 'name': tag.name})


@api_bp.route('/tags/<int:tag_id>', methods=['DELETE'])
def delete_tag(tag_id):
    tag = Tag.query.get_or_404(tag_id)
    db.session.delete(tag)
    db.session.commit()
    return jsonify({'message': '删除成功'})


# ===== Templates =====

@api_bp.route('/templates', methods=['GET'])
def list_templates():
    return jsonify(get_available_templates())


# ===== Generate =====

@api_bp.route('/generate/preview', methods=['POST'])
def preview():
    data = request.get_json() or {}
    card_ids = data.get('card_ids', [])
    template_name = data.get('template', '')

    if not card_ids or not template_name:
        return jsonify({'error': '请选择卡片和模板'}), 400

    cards = Card.query.filter(Card.id.in_(card_ids)).all()
    cards.sort(key=lambda c: card_ids.index(c.id))

    html = generate_site([c.to_dict() for c in cards], template_name)
    return jsonify({'html': html})


@api_bp.route('/generate/export', methods=['POST'])
def export():
    data = request.get_json() or {}
    card_ids = data.get('card_ids', [])
    template_name = data.get('template', '')

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
