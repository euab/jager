import os
import secrets

from random import randint
from discord.ext import commands

IMGUR_ID = os.getenv("IMGUR_ID") or secrets.IMGUR_ID


class DankStuff:
    """Some random stuff made at the request of people"""

    def __init__(self, bot):
        self.bot = bot

    @commands.group()
    async def dank(self, ctx):
        if ctx.invoked_subcommand is None:
            await ctx.send("You will need to give me a "
                           "subcommand. Eg: `!dank kenm`")

    @dank.command()
    async def kenm(self, ctx):
        response = await self.query_imgur(query="kenm")
        await ctx.send(response)

    async def query_imgur(self, query):
        url = "https://api.imgur.com/3/gallery/search"
        headers = {"Authorization": "Client-ID " + IMGUR_ID}
        async with self.bot.session.get(url,
                                        params={'q': query},
                                        headers=headers) as resp:
            data = await resp.json()

        if data["data"]:
            index = randint(1, 50)
            result = data["data"][index]
            response = result["link"]
        else:
            response = "Fuck"

        return response


def setup(bot):
    cog = DankStuff(bot)
    bot.add_cog(cog)