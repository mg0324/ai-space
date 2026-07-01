from flask import Blueprint, render_template, request, redirect, url_for, jsonify
from app import db
from app.models import Tag, Card

tags_bp = Blueprint('tags', __name__)


@tags_bp.route('/')
def list():
    tag_list = Tag.query.order_by(Tag.name).all()
    data = []
    for t in tag_list:
        data.append({
            'tag': t,
            'count': len(t.cards),
        })
    return render_template('tags/list.html', tags=data)


@tags_bp.route('/rename', methods=['POST'])
def rename():
    tag_id = request.form.get('tag_id', type=int)
    new_name = request.form.get('name', '').strip()

    if not tag_id or not new_name:
        return jsonify({'error': '参数错误'}), 400

    tag = Tag.query.get_or_404(tag_id)

    existing = Tag.query.filter(Tag.name == new_name, Tag.id != tag_id).first()
    if existing:
        return jsonify({'error': '标签名已存在'}), 400

    tag.name = new_name
    db.session.commit()
    return jsonify({'message': '重命名成功'})


@tags_bp.route('/delete', methods=['POST'])
def delete():
    tag_id = request.form.get('tag_id', type=int)
    if not tag_id:
        return jsonify({'error': '参数错误'}), 400

    tag = Tag.query.get_or_404(tag_id)
    db.session.delete(tag)
    db.session.commit()
    return jsonify({'message': '删除成功'})


@tags_bp.route('/create', methods=['POST'])
def create():
    name = request.form.get('name', '').strip()
    if not name:
        return jsonify({'error': '标签名不能为空'}), 400

    existing = Tag.query.filter(Tag.name == name).first()
    if existing:
        return jsonify({'error': '标签已存在'}), 400

    tag = Tag(name=name)
    db.session.add(tag)
    db.session.commit()
    return jsonify({'message': '创建成功', 'tag': {'id': tag.id, 'name': tag.name}})
