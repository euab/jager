import inspect
import os

from discord.ext import commands

SOURCE_URL = "https://github.com/euab/the-oofster"

class Source:

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def source(self, ctx, *, command: str = None):
        if command is None:
            return await ctx.send(SOURCE_URL)

        obj = self.bot.get_command(command.replace('.', ' '))
        if obj is None:
            return await ctx.send("Could not find that command")

        src = obj.callback.__code__
        lines, firstlineo = inspect.getsourcelines(src)
        location = os.path.relpath(src.co_filename).replace('\\', '/')
        final_url = f'<{SOURCE_URL}/blob/master/{location}#L{firstlineo}-L{firstlineo + len(lines) - 1}>'
        await ctx.send(final_url)

def setup(bot):
    cog = Source(bot)
    bot.add_cog(cog)