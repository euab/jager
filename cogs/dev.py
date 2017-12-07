import discord

from discord.ext import commands

class Dev:

    def __init__(self, bot):
        self.bot = bot

    @commands.is_owner()
    @commands.command(pass_context=True, no_pm=True)
    async def update_listening(self, ctx, *, listening: str):
        game = discord.Game(name=listening, type=2)
        await self.bot.change_presense(game=game)

def setup(bot):
    cog = Dev(bot)
    bot.add_cog(cog)
