import discord
import json

from discord.ext import commands
from discord.utils import get
from .utils.checks import is_pothead_paradise


class PotheadParadise(commands.Cog):
    def __init__(self, bot):
        self.jager = bot

    @is_pothead_paradise()
    @commands.command()
    async def minecraft(self, ctx):
        """Enables minecraft notifications"""
        member = ctx.message.author
        role = get(member.guild.roles, name='minecraft')

        if role not in ctx.author.roles:
            await member.add_roles(role)
            await ctx.send('**{} Opted you into minecraft ' 
                           'notifications.**'.format(
                               member.mention
            ))

        else:
            await member.remove_roles(role)
            await ctx.send('**{} Opted you out of minecraft '
                           'notifications.**'.format(
                                member.mention
            ))


def setup(bot):
    n = PotheadParadise(bot)
    bot.add_cog(n)