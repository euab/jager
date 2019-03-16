import redis
import asyncio
import logging

log = logging.getLogger(__name__)


class Db(object):
    def __init__(self, redis_url):
        self.redis_url = redis_url
        self.redis = redis.Redis.from_url(redis_url, decode_responses=True)
        