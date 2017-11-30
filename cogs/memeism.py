import discord
import asyncio

from discord.ext import commands

MEMEISM_SERVER_ID = 377838042905313311
MEMEISM_BOT_ROLE_ID = 385114533284544512
MEMEISM_FOLLOWER_ROLE_ID = 377839337158475788

class MemeismExclusive:

    def __init__(self, bot):
        self.bot = bot

    async def on_memeber_join(self, member):
        if member.guild.id is not MEMEISM_SERVER_ID:
            return

        if member.bot:
            await member.add_roles(discord.Object(id=MEMEISM_BOT_ROLE_ID))

        else:
            await member.add_roles(discord.Object(id=MEMEISM_FOLLOWER_ROLE_ID))

def setup(bot):
    cog = MemeismExclusive(bot)
    bot.add_cog(cog)
