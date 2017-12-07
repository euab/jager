import discord
import logging

from discord.ext import commands

log = logging.getLogger('discord')

SUCCESS = '\u2705'
FAILIURE = '\u2049'

class Dev:

    def __init__(self, bot):
        self.bot = bot

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True)
    async def listening(self, ctx, *, listening: str):
        try:
            game = discord.Game(name=listening, type=2)
            await self.bot.change_presence(game=game)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILIURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def load(self, ctx, *, module):
        try:
            self.bot.load_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILIURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def unload(self, ctx, *, module):
        try:
            self.bot.unload_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILIURE)

    @commands.is_owner()
    @commands.command(hidden=True)
    async def reload(self, ctx, *, module):
        try:
            self.bot.unload_extension(module)
            self.bot.load_extension(module)
            await ctx.message.add_reaction(SUCCESS)
        except Exception as e:
            log.error(e)
            await ctx.message.add_reaction(FAILIURE)

def setup(bot):
    cog = Dev(bot)
    bot.add_cog(cog)
