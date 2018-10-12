import discord
import datetime
import traceback
import aiohttp
import psutil
import os
import inspect
import logging
import secrets
import json
import asyncio

from collections import defaultdict
from ext.context import LeContext
from discord.ext import commands
from raven import Client

log = logging.getLogger('discord')


class Jager(commands.AutoShardedBot):
    """Modified commands.AutoShardedBot class"""

    def __init__(self):
        super().__init__(command_prefix=None)
        self.session = aiohttp.ClientSession(loop=self.loop)
        self.uptime = datetime.datetime.utcnow()
        self.commands_used = defaultdict(int)
        self.commands_failed = defaultdict(int)
        self.process = psutil.Process()
        self.messages_sent = 0
        self.sentry = Client(os.getenv("SENTRY_DSN") or secrets.SENTRY_DSN)
        self.load_extensions()
        self._add_commands()
        self.loop = asyncio.get_event_loop()
        self.psa = None

    def _add_commands(self):
        """Adds commands automatically"""
        for attr in inspect.getmembers(self):
            if isinstance(attr, commands.Command):
                self.add_command(attr)

    def load_extensions(self, cogs=None, path='cogs.'):
        """Load cogs into the bot."""
        base_extensions = [x.replace('.py', '') for x in os.listdir('cogs') if x.endswith('.py')]
        for extension in cogs or base_extensions:
            try:
                self.load_extension(f'{path}{extension}')
                log.info(f'Loaded extension {extension}')

            except Exception as e:
                log.info(f'Unable to load extension {extension}. Err: {e}')
                traceback.print_exc()
                self.sentry.captureException()

    @property
    def token(self):
        token = os.getenv("JAGER_TOKEN") or secrets.JAGER_TOKEN
        return token

    @classmethod
    def init(bot, token=None):
        """Start up the bot"""
        bot = Jager()
        token = token or bot.token
        try:
            bot.run(token.strip('"'), bot=True, reconnect=True)
        except Exception as e:
            log.critical("COULD NOT ACCESS TOKEN!")
            log.critical(e)

    async def get_prefix(self, message):
        """Get the prefix for the context's guild"""
        # TODO: Fix the prefix system. It has been temporarily disabled.
        return "!"

    async def create_activity(self):
        """Create standard ticker presence"""
        while True:
            activity = discord.Game(name="!help | Get help")
            await self.change_presence(activity=activity)
            await asyncio.sleep(12.0)
            activity = discord.Streaming(name="Uncompelling gameplay",
                                        url="https://www.twitch.tv/euab")
            await self.change_presence(activity=activity)
            await asyncio.sleep(12.0)
            total_online = len({m.id for m in self.get_all_members() if m.status is not discord.Status.offline})
            total_unique = len(self.users)
            activity = discord.Activity(name=f"{total_online}/{total_unique} users online", type=3)
            await self.change_presence(activity=activity)
            await asyncio.sleep(12.0)
            vc = len(self.voice_clients)
            if vc == 1:
                activity = discord.Activity(name=f"{vc} voice client", type=2)
            else:
                activity = discord.Activity(name=f"{vc} voice clients", type=2)
            await self.change_presence(activity=activity)
            await asyncio.sleep(12.0)

    async def on_connect(self):
        """Event triggered when the bot connects to Discord"""
        log.info("Connected to gateway")

    async def on_shard_connect(self, shard_id):
        """Event triggered when each sharded instance connects"""
        log.info(f"Shard: {shard_id} connected... ")

    async def on_ready(self):
        """Triggered when the bot has completed startup"""
        log.info("Ready. Yay.")
        await self.create_activity()
        self.test_sentry()

        # ready ascii
        with open("ready_ascii.txt") as f:
            print(f'\n{f.read()}')

    async def on_command(self, ctx):
        """Triggered whenever a command is invoked"""
        cmd = ctx.command.qualified_name.replace(' ', '_')
        log.info("Invoked: {}@{} >> {}".format(
            ctx.message.author,
            ctx.guild.name,
            ctx.command.qualified_name
        ))
        self.commands_used[cmd] += 1

    async def on_command_error(self, ctx, error):
        """Whenever a command fails"""
        log.error(error)
        traceback.print_exc()
        cmd = ctx.command.qualified_name.replace(' ', '_')
        self.commands_failed[cmd] += 1

    async def process_commands(self, message):
        """Process the command"""
        ctx = await self.get_context(message, cls=LeContext)  # Le Meme
        if ctx.command is None:
            return

        await self.invoke(ctx)

    async def on_message(self, message):
        """The bot's actual listener. Triggered whenever any message is sent."""
        self.messages_sent += 1
        if message.author.bot:
            return
        await self.process_commands(message)

    def test_sentry(self):
        """Test Sentry by creating an error and having the DSN capture it"""
        try:
            1 / 0
        except ZeroDivisionError:
            self.sentry.captureException()


def main():
    """Initialise logger and run init func"""
    token = os.getenv("TOKEN") or secrets.JAGER_TOKEN
    logging.basicConfig(filename="log.log",
                        filemode="w",
                        format="[%(asctime)s] %(msecs)d %(name)s %(levelname)s %(message)s",
                        datefmt="'%H:%M:%S'",
                        level=logging.INFO)
    Jager.init(token)


if __name__ == '__main__':
    main()
