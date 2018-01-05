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

from collections import defaultdict
from ext.context import LeContext
from discord.ext import commands

log = logging.getLogger('discord')


class RickBot(commands.AutoShardedBot):
    """Modified commands.AutoShardedBot class"""

    def __init__(self):
        super().__init__(command_prefix=None)
        self.session = aiohttp.ClientSession(loop=self.loop)
        self.uptime = datetime.datetime.utcnow()
        self.commands_used = defaultdict(int)
        self.commands_failed = defaultdict(int)
        self.process = psutil.Process()
        self.messages_sent = 0
        self.load_extensions()
        self._add_commands()

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

    @property
    def token(self):
        token = os.getenv("TOKEN") or secrets.TOKEN
        return token

    @classmethod
    def init(bot, token=None):
        """Start up the bot"""
        bot = RickBot()
        token = token or bot.token
        try:
            bot.run(token.strip('"'), bot=True, reconnect=True)
        except Exception as e:
            log.critical("COULD NOT ACCESS TOKEN!")
            log.critical(e)

    async def get_prefix(self, message):
        """Get the prefix for the context's guild"""
        with open('data/guild.json') as f:
            data = json.load(f)

        gid = str(getattr(message.guild, 'id', None))

        prefixes = [
            f'<@{self.user.id}> ',
            f'<@!{self.user.id}> ',
            data.get(gid, '!')
        ]

        return prefixes

    async def create_presence(self):
        game = discord.Game(name="!help", type=1,
                            url="https://www.twitch.tv/euab")
        await self.change_presence(game=game)

    async def on_connect(self):
        """Event triggered when the bot connects to Discord"""
        log.info("Connected to gateway")

    async def on_shard_connect(self, shard_id):
        """Event triggered when each sharded instance connects"""
        log.info(f"Shard: {shard_id} connected... ")

    async def on_ready(self):
        """Triggered when the bot has completed startup"""
        log.info("Ready. Yay.")
        self.clean_cache_job()
        await self.create_presence()

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
        cmd = ctx.command.qualified_name.replace(' ', '_')
        self.commands_failed[cmd] += 1

        if isinstance(error, commands.BadArgument):
            msg = error
        elif isinstance(error, commands.CommandInvokeError):
            original = error.original
            if isinstance(original, discord.Forbidden):
                msg = "I do not have permission to " \
                      "do that."
            elif isinstance(original, discord.HTTPException):
                msg = "It appears that something has " \
                      "gone wrong over the line between " \
                      "me and Discord. Try again later?"

        msg = msg + "\nhttps://imgur.com/rlsPsfX"
        await ctx.send(msg)

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

    @staticmethod
    def clean_cache_job():
        """Job which cleans the bot's cache"""
        os.system("rm -r cache")
        os.system("mkdir cache")
        os.system("cd cache && mkdir voice")
        os.system("cd ..")


def main():
    """Initialise logger and run init func"""
    token = os.getenv("TOKEN") or secrets.TOKEN
    logging.basicConfig(level="INFO")
    RickBot.init(token)


if __name__ == '__main__':
    main()
