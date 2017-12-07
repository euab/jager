import discord
import logging

from discord.ext import commands

log = logging.getLogger('discord')

class Dev:

    def __init__(self, bot):
        self.bot = bot

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True)
    async def update_listening(self, ctx, *, listening: str):
        game = discord.Game(name=listening, type=2)
        await self.bot.change_presence(game=game)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def load(self, ctx, *, module):
        try:
            self.bot.load_extension(module)
            await ctx.message.add_reaction('\u2705')
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction('\u2049')

    @commands.is_owner()
    @commands.command(hidden=True)
    async def unload(self, ctx, *, module):
        try:
            self.bot.unload_extension(module)
            await ctx.message.add_reaction('\u2705')
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction('\u2049')

def setup(bot):
    cog = Dev(bot)
    bot.add_cog(cog)
