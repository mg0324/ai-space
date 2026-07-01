import json
from datetime import datetime
from flask import Blueprint, render_template, request, redirect, url_for, jsonify
from app import db
from app.models import Card, Tag

cards_bp = Blueprint('cards', __name__)


@cards_bp.route('/')
def list():
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
    all_tags = Tag.query.order_by(Tag.name).all()

    return render_template('cards/list.html', cards=cards, tags=all_tags,
                           current_tag=tag_name, search=search)


@cards_bp.route('/new', methods=['GET', 'POST'])
def new():
    if request.method == 'POST':
        title = request.form['title'].strip()
        content = request.form['content'].strip()
        source = request.form.get('source', '').strip()
        tag_ids = request.form.getlist('tag_ids', type=int)

        if not title or not content:
            all_tags = Tag.query.order_by(Tag.name).all()
            return render_template('cards/form.html', error='标题和内容不能为空',
                                   card=request.form, all_tags=all_tags,
                                   selected_tag_ids=tag_ids)

        card = Card(title=title, content=content, source=source)
        if tag_ids:
            card.tag_list = Tag.query.filter(Tag.id.in_(tag_ids)).all()

        db.session.add(card)
        db.session.commit()
        return redirect(url_for('cards.list'))

    all_tags = Tag.query.order_by(Tag.name).all()
    return render_template('cards/form.html', card=None, all_tags=all_tags,
                           selected_tag_ids=[])


@cards_bp.route('/<int:card_id>/edit', methods=['GET', 'POST'])
def edit(card_id):
    card = Card.query.get_or_404(card_id)
    all_tags = Tag.query.order_by(Tag.name).all()

    if request.method == 'POST':
        card.title = request.form['title'].strip()
        card.content = request.form['content'].strip()
        card.source = request.form.get('source', '').strip()
        tag_ids = request.form.getlist('tag_ids', type=int)

        if not card.title or not card.content:
            return render_template('cards/form.html', error='标题和内容不能为空',
                                   card=card, all_tags=all_tags,
                                   selected_tag_ids=tag_ids)

        if tag_ids:
            card.tag_list = Tag.query.filter(Tag.id.in_(tag_ids)).all()
        else:
            card.tag_list = []

        db.session.commit()
        return redirect(url_for('cards.list'))

    selected_tag_ids = [t.id for t in card.tag_list]
    return render_template('cards/form.html', card=card, all_tags=all_tags,
                           selected_tag_ids=selected_tag_ids)


@cards_bp.route('/<int:card_id>/delete', methods=['POST'])
def delete(card_id):
    card = Card.query.get_or_404(card_id)
    db.session.delete(card)
    db.session.commit()
    return redirect(url_for('cards.list'))


@cards_bp.route('/<int:card_id>')
def detail(card_id):
    card = Card.query.get_or_404(card_id)
    return render_template('cards/detail.html', card=card)


@cards_bp.route('/export')
def export_cards():
    cards = Card.query.all()
    data = [c.to_dict() for c in cards]
    return jsonify(data)


@cards_bp.route('/import', methods=['POST'])
def import_cards():
    file = request.files.get('file')
    if not file:
        return redirect(url_for('cards.list'))

    try:
        data = json.loads(file.read().decode('utf-8'))
    except (json.JSONDecodeError, UnicodeDecodeError):
        return redirect(url_for('cards.list'))

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
    return redirect(url_for('cards.list'))
