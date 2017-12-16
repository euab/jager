import asyncio
import logging

from functools import wraps

log = logging.getLogger(__name__)

def bg_task(sleep_time, ignore_errors=True):
    def actual_decorator(func):
        @wraps(func)
        async def wrapper(self):
            await self.bot.wait_until_ready()
            while True:
                if ignore_errors:
                    try:
                        await func(self)
                    except Exception as e:
                        log.info("An error occurred in {} the bg "
                                 " task. Retrying in {} seconds.".format(
                                     func.__name__,
                                     sleep_time
                                 ))
                        log.info(e)
                else:
                    await func(self)

                await asyncio.sleep(sleep_time)

        wrapper._bg_task = True
        return wrapper

    return actual_decorator