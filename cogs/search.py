import os
import aiohttp
import discord
import secrets

from discord.ext import commands
from lxml import etree

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY") or secrets.GOOGLE_API_KEY
TWITCH_CLIENT_ID = os.getenv("TWITCH_CLIENT_ID") or secrets.TWITCH_CLIENT_ID
IMGUR_ID = os.getenv("IMGUR_ID") or secrets.IMGUR_ID

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
            em = discord.Embed()
            em.set_thumbnail(url="http://www.extension.zone/wp-content"
                                 "/uploads/2015/11/Urban-Dictionary-logo.png")
            em.color = 0xCC3C32
            em.title = entry["word"]
            em.description = entry["definition"]
            em.url = entry["permalink"]
            em.add_field(name="example", value=entry["example"],
                         inline=True)

            await ctx.send(embed=em)

        else:
            await ctx.send(NOT_FOUND)

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

    @commands.command()
    async def cpp(self, ctx, *, query: str):
        url = 'http://en.cppreference.com/w/cpp/index.php'
        params = {
            'title': 'Special:Search',
            'search': query
        }

        async with self.bot.session.get(url, params=params) as resp:
            if resp.status != 200:
                return await ctx.send(f'An error has occurred. '
                                      '(error code: {resp.status}). '
                                      'Retry later maybe?')
            if len(resp.history) > 0:
                return await ctx.send(resp.url)

            em = discord.Embed()
            root = etree.fromstring(await resp.text(), etree.HTMLParser())

            nodes = root.findall(".//div[@class='mw-search-result-heading']/a")

            description = []
            special_pages = []
            for node in nodes:
                href = node.attrib['href']
                if not href.startswith('/w/cpp'):
                    continue

                if href.startswith(('/w/cpp/language', '/w/cpp/concept')):
                    special_pages.append(f'[{node.text}](http://en.cppreference.com{href})')
                else:
                    description.append(f'[`{node.text}`](http://en.cppreference.com{href})')

            if len(special_pages) > 0:
                em.add_field(name='Language Results', value='\n'.join(special_pages), inline=False)
                if len(description):
                    em.add_field(name="Library Results", value='\n'.join(description[:10]), inline=False)

            else:
                if not len(description):
                    return await ctx.send(NOT_FOUND)

                em.title = 'Search Results'
                em.description = '\n'.join(description[:15])

            await ctx.send(embed=em)

    @commands.command()
    async def imgur(self, ctx, *, search: str):
        url = "https://api.imgur.com/3/gallery/search/viral"
        headers = {"Authorization": "Client-ID " + IMGUR_ID}
        async with self.bot.session.get(url,
                                        params={"q": search},
                                        headers=headers) as resp:
            data = await resp.json()

        if data["data"]:
            result = data["data"][0]
            response = result["link"]
        else:
            response = NOT_FOUND

        await ctx.send(response)


def setup(bot):
    cog = Search(bot)
    bot.add_cog(cog)
