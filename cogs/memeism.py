import discord
import os
import secrets

from random import randint
from discord.ext import commands

MEMEISM_SERVER_ID        = 377838042905313311
MEMEISM_BOT_ROLE_ID      = 385114533284544512
MEMEISM_FOLLOWER_ROLE_ID = 377839337158475788
MEMEISM_MAIN_CHANNEL_ID  = 377838043345977355

IMGUR_ID = os.getenv("IMGUR_ID") or secrets.IMGUR_ID


class MemeismExclusive:

    def __init__(self, bot):
        self.bot = bot

    async def member_autorole(self, member):
        if member.guild.id is not MEMEISM_SERVER_ID:
            return

        if member.bot:
            await member.add_roles(discord.Object(id=MEMEISM_BOT_ROLE_ID))

        else:
            await member.add_roles(discord.Object(id=MEMEISM_FOLLOWER_ROLE_ID))

    @commands.command()
    async def kenm(self, ctx):
        url = "https://api.imgur.com/3/gallery/search"
        headers = {"Authorization": "Client-ID " + IMGUR_ID}
        async with self.bot.session.get(url,
                                        params={'q': 'ken m'},
                                        headers=headers) as resp:
            data = await resp.json()

        if data["data"]:
            index = randint(1, 50)
            result = data["data"][index]
            response = result["link"]
        else:
            response = "Fuck"

        await ctx.send(response)


def setup(bot):
    cog = MemeismExclusive(bot)
    bot.add_listener(cog.member_autorole, "on_member_join")
    bot.add_cog(cog)
