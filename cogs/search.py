import os
import aiohttp
import discord
import secrets

from discord.ext import commands

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
TWITCH_CLIENT_ID = os.getenv("TWITCH_CLIENT_ID")

NOT_FOUND = "I couldn't find anything 😢"

class Search:

    def __init__(self, bot):
        self.bot = bot
        self.session = aiohttp.ClientSession()

    @commands.command(pass_context=True, no_pm=True)
    async def youtube(self, ctx, *, search: str):
        url = "https://www.googleapis.com/youtube/v3/search"
        async with self.session.get(url, params={"type": "video",
                                                 "q": search,
                                                 "part": "snippet",
                                                 "key": GOOGLE_API_KEY}) as resp:
            data = await resp.json()
        if data["items"]:
            video = data["items"][0]
            response = "https://youtu.be/" + video["id"]["videoId"]
        else:
            response = NOT_FOUND

        await ctx.send(response)

    @commands.command(pass_context=True, no_pm=True)
    async def urban(self, ctx, *, search: str):
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

    @commands.command(pass_context=True, no_pm=True)
    async def twitch(self, ctx, *, search: str):
        url = "https://api.twitch.tv/kraken/search/channels"
        params = {
            "q": search,
            "client_id": TWITCH_CLIENT_ID
        }
        async with self.session.get(url, params=params) as resp:
            data = await resp.json()

        if data["channels"]:
            channel = data["channels"][0]
            fmt = "{0[followers]} followers and {0[views]} views"
            em = discord.Embed()
            em.color = discord.Color.purple()
            em.title = channel["display_name"]
            em.add_field(name="Followers & co", value=fmt.format(channel))
            em.set_footer(text=f'{channel["url"]}')
            try:
                await ctx.send(embed=em)
            except discord.Forbidden:
                await ctx.send("I am not allowed to send embeds in your server... :cry:")

        else:
            ctx.send(NOT_FOUND)

def setup(bot):
    cog = Search(bot)
    bot.add_cog(cog)
