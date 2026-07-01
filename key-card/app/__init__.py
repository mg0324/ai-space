import os
from flask import Flask
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


def create_app(test_config=None):
    app = Flask(__name__, instance_relative_config=True)
    app.config.from_object('config.Config')

    if test_config:
        app.config.update(test_config)

    os.makedirs(app.instance_path, exist_ok=True)

    db.init_app(app)

    from app.routes.cards import cards_bp
    from app.routes.templates import templates_bp
    from app.routes.generate import generate_bp
    from app.routes.main import main_bp

    app.register_blueprint(main_bp)
    app.register_blueprint(cards_bp, url_prefix='/cards')
    app.register_blueprint(templates_bp, url_prefix='/templates')
    app.register_blueprint(generate_bp, url_prefix='/generate')

    with app.app_context():
        from app.models import Card
        db.create_all()

    return app
