import discord
import asyncio

from discord.ext import commands

class Mod:

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    @commands.guild_only()
    @commands.has_permissions(manage_guild=True)
    async def clear(self, ctx, *, value: int):
        await ctx.send("Gettin' ready to purge")
        clear = await ctx.channel.purge(limit=value,
                                        check=None)

        amount = len(clear)
        await ctx.send(f"`Cleared {amount} messages`", delete_after=5)

def setup(bot):
    cog = Mod(bot)
    bot.add_cog(cog)
