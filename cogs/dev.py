import discord
import logging
import asyncio
import inspect
import traceback
import io
import os

from discord.ext import commands
from contextlib import redirect_stdout

log = logging.getLogger(__name__)

SUCCESS = '\N{WHITE HEAVY CHECK MARK}'
FAILURE = '\N{WARNING SIGN}'


class Dev:

    def __init__(self, bot):
        self.bot = bot
        self.sessions = set()

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True, hidden=True)
    async def listening(self, ctx, *, listening: str):
        try:
            activity = discord.Activity(name=listening, type=2)
            await self.bot.change_presence(activity=activity)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True, hidden=True)
    async def playing(self, ctx, *, playing: str):
        try:
            activity = discord.Game(name=playing)
            await self.bot.change_presence(activity=activity)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True, hidden=True)
    async def watching(self, ctx, *, watching: str):
        try:
            activity = discord.Activity(name=watching, type=3)
            await self.bot.change_presence(activity=activity)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True, hidden=True)
    async def streaming(self, ctx, *, playing: str):
        try:
            activity = discord.Streaming(name=playing, url="https://www.twitch.tv/euab")
            await self.bot.change_presence(activity=activity)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def load(self, ctx, *, module):
        try:
            self.bot.load_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def unload(self, ctx, *, module):
        try:
            self.bot.unload_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def reload(self, ctx, *, module):
        try:
            self.bot.unload_extension(module)
            self.bot.load_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILURE)

    @commands.command(hidden=True)
    @commands.is_owner()
    async def update(self, ctx):
        msg = "<a:updating:403035325242540032> **Pulling from repository and " \
              "restarting shards.**"
        await ctx.send(msg)
        command = 'sh update.sh'
        os.system(command)
        # Gracefully exit the bot
        await self.bot.logout()

    @commands.command(hidden=True)
    @commands.is_owner()
    async def logs(self, ctx):
        file = discord.File(fp="log.log")
        await ctx.send(":white_check_mark: **Retrieved logs**",
                       file=file)

    @commands.command(pass_context=True)
    async def repl(self, ctx):
        log.info(f"A REPL session has started in {ctx.guild}->{ctx.channel}")

        variables = {
            'ctx': ctx,
            'bot': self.bot,
            'message': ctx.message,
            'guild': ctx.guild,
            'channel': ctx.channel,
            'author': ctx.author,
            '_': None,
        }

        if ctx.channel.id in self.sessions:
            await ctx.send("There is already a REPL session running in this channel. Exit it with `quit`.")
            return

        self.sessions.add(ctx.channel.id)
        await ctx.send("REPL session is now running. Enter code to execute or evaluate. Exit using `exit()` or `quit`.")

        def check(m):
            return m.author.id == ctx.author.id and \
                   m.channel.id == ctx.channel.id and \
                   m.content.startswith('`')

        while True:
            try:
                response = await self.bot.wait_for('message', check=check, timeout=10.0*60.0)
            except asyncio.TimeoutError:
                await ctx.send("Exiting REPL session and popping ID")
                self.sessions.remove(ctx.channel.id)
                break

            cleaned = cleanup_code(response.content)

            if cleaned in ('quit', 'exit', 'exit()'):
                await ctx.send("Exiting REPL session and popping ID")
                self.sessions.remove(ctx.channel.id)
                return

            executor = exec
            if cleaned.count('\n') == 0:
                try:
                    code = compile(cleaned, '<repl session>', 'eval')
                except SyntaxError:
                    pass
                else:
                    executor = eval

            if executor is exec:
                try:
                    code = compile(cleaned, '<repl session>', 'exec')
                except SyntaxError as e:
                    await ctx.send(get_syntax_error(e))
                    continue

            variables['message'] = response

            fmt = None
            stdout = io.StringIO()

            try:
                with redirect_stdout(stdout):
                    result = executor(code, variables)
                    if inspect.isawaitable(result):
                        result = await result
            except Exception as e:
                value = stdout.getvalue()
                fmt = f'```py\n{value}{traceback.format_exc()}\n```'
            else:
                value = stdout.getvalue()
                if result is not None:
                    fmt = f'```py\n{value}{result}\n```'
                    variables['_'] = result
                elif value:
                    fmt = f'```py\n{value}\n```'

            try:
                if fmt is not None:
                    if len(fmt) > 2000:
                        await ctx.send('Content too big to be printed.')
                    else:
                        await ctx.send(fmt)
            except discord.Forbidden:
                pass
            except discord.HTTPException as e:
                await ctx.send(f'Unexpected error: `{e}`')


def cleanup_code(content):
    if content.startswith('```') and content.endswith('```'):
        return '\n'.join(content.split('\n')[1:-1])

    return content.strip('` \n')


def get_syntax_error(e):
    if e.text is None:
        return f'```py\n{e.__class__.__name__}: {e}\n```'
    return f'```py\n{e.text}{"^":>{e.offset}}\n{e.__class__.__name__}: {e}```'


def setup(bot):
    cog = Dev(bot)
    bot.add_cog(cog)
