import os
import redis

from functools import wraps
from requests_oauthlib import OAuth2Session
from flask import Flask, session, url_for, request, render_template, \
redirect, jsonify


app = Flask(__name__)
app.debug = True
app.config['SECRET_KEY'] = os.urandom(21)

OAUTH2_CLIENT_ID = os.environ['OAUTH2_CLIENT_ID']
OAUTH2_CLIENT_SECRET = os.environ['OAUTH2_CLIENT_SECRET']
OAUTH2_REDIRECT_URI = 'localhost:5000/validate_login'
API_BASE_URL = 'https://discordapp.com/api'
AUTHORIZATION_BASE_URL = API_BASE_URL + '/oauth2/authorize'
TOKEN_URL = API_BASE_URL + '/oauth2/token'
INVITE_URL = "https://discordapp.com/oauth2/authorize?&client_id={}" \
             "&scope=bot&permissions={}&guild_id={}"
REDIS_URL = os.environ.get('REDIS_URL')

os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'

db = redis.Redis.from_url(REDIS_URL, decode_responses=True)


def token_update(token):
    session['oauth2_token'] = token

def require_auth(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        user = session.get('user')
        if user is None:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return wrapper

def create_session(token=None, state=None, scope=None):
    return OAuth2Session(
        client_id = OAUTH2_CLIENT_ID,
        token=token,
        state=state,
        scope=scope,
        redirect_uri=OAUTH2_REDIRECT_URI,
        auto_refresh_kwargs={
            'client_id': OAUTH2_CLIENT_ID,
            'client_secret': OAUTH2_CLIENT_SECRET
        },
        auto_refresh_uri=TOKEN_URL,
        token_updater=token_update
    )

@app.route('/')
def index():
    user = session.get('user')
    if user is not None:
        return redirect(url_for('select_guild'))

    return render_template('index.html')

@app.route('/login')
def login():
    user = session.get('user')
    if user is not None:
        return redirect(url_for('select_guild'))

    scope = 'identify guilds'.split()
    discord = create_session(scope=scope)
    authorization_url, state = discord.authorization_url(
        AUTHORIZATION_BASE_URL
    )
    session['oauth2_state'] = state
    return(redirect(authorization_url))

@app.route('/validate_login')
def validate_login():
    if request.values.get('error'):
        return redirect(url_for('index'))

    discord = create_session(state=session.get('oauth2_state'))
    token = discord.fetch_token(
        TOKEN_URL,
        client_secret=OAUTH2_CLIENT_SECRET,
        authorization_response=request.url
    )

    session['oauth2_token'] = token
    get_or_update_user()

    return redirect(url_for('select_guild'))

def get_or_update_user():
    oauth2_token = session.get('oauth2_token')
    if oauth2_token:
        discord = create_session(token=oauth2_token)
        session['user'] = discord.get(API_BASE_URL, '/users/@me').json()
        session['guilds'] = discord.get(API_BASE_URL, '/users/@me').json()

def get_user_server(user, guilds):
    return list(filter(lambda g: g['owner'] is True, guilds))

@app.route('/guilds')
@require_auth
def select_guild():
    get_or_update_user()
    user_guilds = get_user_server(session['user'], session['guilds'])

    return render_template('select_guild.html', user_guilds=user_guilds)

def guild_check(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        guild_id = args[0]
        guild_ids = redis.smembers('guild')

        if guild_id not in guild_ids:
            url = INVITE_URL.fmt(
                OAUTH2_CLIENT_ID,
                '1'*32,
                guild_id
            )
            return redirect(url)
        return f(*args, **kwargs)
    return wrapper

def require_admin_privileges(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        guild_id = args[0]
        user_guilds = get_user_server(session['user'], session['guilds'])
        if guild_id not in list(map(lambda g: g['id'], user_guilds)):
            return redirect(url_for('select_guild'))
        return f(*args, **kwargs)
    return wrapper

@app.route('/dashboard/<int:guild_id>')
@require_auth
@guild_check
@require_admin_privileges
def dashboard(guild_id):
    return "Welcome to guild: " + guild_id


if __name__ == '__main__':
    app.run()
