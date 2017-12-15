import discord
import logging

from discord.ext import commands

RICKBOT_SERVER_ID  = 383914538640343040
NEW_MEMBER_ROLE_ID = 391253589193916416

log = logging.getLogger(__name__)

class RickbotServer:

    def __init__(self, bot):
        self.bot = bot

    async def new_member_autorole(self, member):
        if member.guild.id != RICKBOT_SERVER_ID:
            return

        if member.bot:
            return

        else:
            await member.add_roles(discord.Object(id=NEW_MEMBER_ROLE_ID))

    @commands.command(hidden=True)
    @commands.check(lambda ctx: ctx.guild and ctx.guild.id == RICKBOT_SERVER_ID)
    async def iagree(self, ctx):
        if not any(r.id == NEW_MEMBER_ROLE_ID for r in ctx.author.roles):
            # Has to be something that python can convert into Unicode.
            return await ctx.message.add_reaction('\N{WARNING SIGN}')

        try:
            await ctx.author.remove_roles(discord.Object(id=NEW_MEMBER_ROLE_ID))
        except Exception as e:
            log.warning(e)
            await ctx.message.add_reaction('\N{NO ENTRY SIGN}')
        else:
            await ctx.message.add_reaction('\N{WHITE HEAVY CHECK MARK}')

def setup(bot):
    cog = RickbotServer(bot)
    bot.add_listener(cog.new_member_autorole, "on_member_join")
    bot.add_cog(cog)