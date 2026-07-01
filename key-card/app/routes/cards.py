import json
from datetime import datetime
from flask import Blueprint, render_template, request, redirect, url_for, jsonify
from app import db
from app.models import Card

cards_bp = Blueprint('cards', __name__)


@cards_bp.route('/')
def list():
    tag = request.args.get('tag')
    search = request.args.get('q')
    query = Card.query.order_by(Card.updated_at.desc())

    if tag:
        query = query.filter(Card.tags.contains(tag))
    if search:
        query = query.filter(
            Card.title.contains(search) | Card.content.contains(search)
        )

    cards = query.all()
    all_tags = set()
    for c in Card.query.all():
        for t in c.tags.split(',') if c.tags else []:
            t = t.strip()
            if t:
                all_tags.add(t)

    return render_template('cards/list.html', cards=cards, tags=sorted(all_tags),
                           current_tag=tag, search=search)


@cards_bp.route('/new', methods=['GET', 'POST'])
def new():
    if request.method == 'POST':
        title = request.form['title'].strip()
        content = request.form['content'].strip()
        tags = request.form.get('tags', '').strip()
        source = request.form.get('source', '').strip()

        if not title or not content:
            return render_template('cards/form.html', error='标题和内容不能为空',
                                   card=request.form)

        card = Card(title=title, content=content, tags=tags, source=source)
        db.session.add(card)
        db.session.commit()
        return redirect(url_for('cards.list'))

    return render_template('cards/form.html', card=None)


@cards_bp.route('/<int:card_id>/edit', methods=['GET', 'POST'])
def edit(card_id):
    card = Card.query.get_or_404(card_id)

    if request.method == 'POST':
        card.title = request.form['title'].strip()
        card.content = request.form['content'].strip()
        card.tags = request.form.get('tags', '').strip()
        card.source = request.form.get('source', '').strip()
        card.updated_at = datetime.utcnow()

        if not card.title or not card.content:
            return render_template('cards/form.html', error='标题和内容不能为空',
                                   card=card)

        db.session.commit()
        return redirect(url_for('cards.list'))

    return render_template('cards/form.html', card=card)


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
            tags=item.get('tags', ''),
            source=item.get('source', ''),
        )
        db.session.add(card)
        count += 1

    db.session.commit()
    return redirect(url_for('cards.list'))
