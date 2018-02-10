import asyncio
import logging
import discord
import youtube_dl

from discord.ext import commands

DEV_SERVER_ID = 366583119622569986

log = logging.getLogger(__name__)

try:
    if not discord.opus.is_loaded:
        discord.opus.load_opus('libopus.so')
except OSError:
    log.info("pleb")
except Exception as e:
    log.error(e)

youtube_dl.utils.bug_reports_message = lambda: ''

ytdl_format_options = {
    'format': 'bestaudio/best',
    'outtmpl': 'cache/voice/%(extractor)s-%(id)s-%(title)s.%(ext)s',
    'restrictfilenames': True,
    'noplaylist': True,
    'nocheckcertificate': True,
    'ignoreerrors': False,
    'logtostderr': False,
    'quiet': True,
    'no_warnings': True,
    'default_search': 'auto',
    'source_address': '0.0.0.0'
}

ffmpeg_options = {
    'before_options': '-nostdin',
    'options': '-vn'
}

ytdl = youtube_dl.YoutubeDL(ytdl_format_options)


class YTDLSource(discord.PCMVolumeTransformer):

    def __init__(self, source, *, data, volume=0.5):
        super().__init__(source, volume)

        self.data = data

        self.title = data.get('title')
        self.url = data.get('url')

    @classmethod
    async def from_url(cls, url, *, loop=None):
        loop = loop or asyncio.get_event_loop()
        data = await loop.run_in_executor(None, ytdl.extract_info, url)
        
        if 'entries' in data:
            data = data['entries'][0]

        filename = ytdl.prepare_filename(data)
        return cls(discord.FFmpegPCMAudio(filename, **ffmpeg_options), data=data)


class Music:

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def join(self, ctx, *, channel: discord.VoiceChannel):
        if ctx.voice_client is not None:
            return await ctx.voice_client.move_to(channel)
        
        await channel.connect()

    @commands.command()
    async def play(self, ctx, *, url):
        # TODO: Check the video data for a stream index. Don't know one is provided. Will find out.
        if "stream" in url:
            return await ctx.send("**No**")
        async with ctx.typing():
            if ctx.voice_client is None:
                if ctx.author.voice.channel:
                    await ctx.author.voice.channel.connect()
                else:
                    return await ctx.send("I'm not connected to a voice channel... :grimacing:")

            if ctx.voice_client.is_playing():
                ctx.voice_client.stop()

            player = await YTDLSource.from_url(url, loop=self.bot.loop)
            ctx.voice_client.play(player, after=lambda e: print('Something went wrong here... :cry:') if e else None)

            if ctx.guild.id == DEV_SERVER_ID:
                game = discord.Game(name=player.title, type=2)
                await self.bot.change_presence(game=game)
            
            await ctx.send('Now playing **{}** :ok_hand:'.format(player.title))

    @commands.command()
    async def nonce(self, ctx):
        async with ctx.typing():
            url = "https://www.youtube.com/watch?v=C5eUJZWs-2k"
            if ctx.author.voice.channel:
                await ctx.author.voice.channel.connect()
            else:
                return await ctx.send("I am not connected to a voice channel you nonce.")

            if ctx.voice_client.is_playing():
                ctx.voice_client.stop()

            player = await YTDLSource.from_url(url, loop=self.bot.loop)
            ctx.voice_client.play(player, after=lambda e: print('Something went wrong... :cry:') if e else None)

            await ctx.send('http://gph.is/2zsPLKf')

    @commands.command()
    async def volume(self, ctx, volume: int):
        if ctx.voice_client is None:
            return await ctx.send("I'm not connected to a voice channel... :grimacing:")

        ctx.voice_client.source.volume = volume
        await ctx.send("Changed player volume to {}%".format(volume))


    @commands.command()
    async def stop(self, ctx):
        await ctx.voice_client.disconnect()
        if ctx.guild.id == DEV_SERVER_ID:
            await self.bot.create_presence()

def setup(bot):
    cog = Music(bot)
    bot.add_cog(cog)