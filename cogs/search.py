import os
import aiohttp
import discord
import secrets

from discord.ext import commands

GOOGLE_API_KEY = secrets.GOOGLE_API_KEY

NOT_FOUND = "I couldn't find anything 😢"

class Search:

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def youtube(self, ctx):
        search = ctx
        async with self.session.get(url, params={"type": "video",
                                                 "q": search,
                                                 "part": snippet,
                                                 "key": GOOGLE_API_KEY}) as resp:
            data = await resp.json()
        if data["items"]:
            video = data["items"][0]
            response = "https://youtu.be/" + video["id"]["videoId"]
        else:
            response = NOT_FOUND

        await ctx.send(response)

    @commands.command()
    async def urban(self, ctx):
        search = ctx
        url = "http://api.urbandictionary.com/v0/define"
        async with self.session.get(url, params={"term": search}) as resp:
            data = await resp.json()

        if data["list"]:
            entry = data["list"][0]
            fmt = "\n **{e[word]}** ```\n{e[definition]}``` \n " \
                  "**example: {e[example]} \n" \
                  "<{e[permalink]}>"
            response = fmt.format(e=entry)
        else:
            response = NOT_FOUND
        await ctx.send(response)

def setup(bot):
    cog = Search(bot)
    bot.add_cog(cog)
