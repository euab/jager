import discord
import asyncio

from discord.ext import commands

class Mod:

    def __init__(self, bot):
        self.bot = bot

    @commands.command
    async def clear(self, ctx, *, amount: str):
        await ctx.send("Gettin' ready to purge")
        clear = await ctx.purge_from(ctx.channel,
                                     limit=amount,
                                     check=check)
                                     
        await ctx.send(f"`Clear {len(clear) messages}`)

def setup(bot):
    cog = Mod(bot)
    bot.add_cog(cog)
