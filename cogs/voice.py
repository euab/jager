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
            await ctx.voice_client.move_to(channel)
            return await ctx.send(f"Moved to **{channel}** \N{MULTIPLE MUSICAL NOTES} \N{OK HAND SIGN}")

        await channel.connect()
        await ctx.send(f"Joined **{channel}** \N{MULTIPLE MUSICAL NOTES} \N{OK HAND SIGN}")


    @commands.command()
    async def play(self, ctx, *, url):
        # TODO: Check the video data for a stream index. Don't know one is provided. Will find out.
        if "stream" in url:
            return await ctx.send("**No**")

        async with ctx.typing():
            if ctx.voice_client is None:
                if ctx.author.voice.channel:
                    await ctx.author.voice.channel.connect()
                    await ctx.send(f"Connecting to **{ctx.author.voice.channel.name}** "
                                    "\N{MULTIPLE MUSICAL NOTES} \N{OK HAND SIGN}")
                else:
                    return await ctx.send("I'm not connected to a voice channel... :grimacing:")

            if ctx.voice_client.is_playing():
                ctx.voice_client.stop()

            player = await YTDLSource.from_url(url, loop=self.bot.loop)
            ctx.voice_client.play(player, after=lambda e: print('Something went wrong here... :cry:') if e else None)

            if ctx.guild.id == DEV_SERVER_ID:
                activity = discord.Activity(name=player.title, type=2)
                await self.bot.change_presence(activity=activity)

            await ctx.send('Now playing **{}** :ok_hand:'.format(player.title))

    @commands.command()
    async def pause(self, ctx):
        if ctx.voice_client.is_playing():
            ctx.voice_client.pause()
            await ctx.send("**Paused** \N{DOUBLE VERTICAL BAR}")
        else:
            return await ctx.send("**Not playing anything.** Use `{}play` to play something."
                                  "".format(ctx.prefix))

    @commands.command()
    async def resume(self, ctx):
        if ctx.voice_client.is_paused():
            ctx.voice_client.resume()
            await ctx.send("**Resumed** \N{BLACK RIGHT-POINTING TRIANGLE}")
        else:
            return await ctx.send("**Not paused.** Use `{}pause` to pause."
                                  "".format(ctx.prefix))

    @commands.command()
    async def volume(self, ctx, volume: int):
        if ctx.voice_client is None:
            return await ctx.send("I'm not connected to a voice channel... :grimacing:")

        ctx.voice_client.source.volume = volume
        await ctx.send("Changed player volume to {}%".format(volume))

    @commands.command(aliases=["fuck off"])
    async def stop(self, ctx):
        await ctx.voice_client.disconnect()
        if ctx.guild.id == DEV_SERVER_ID:
            await ctx.send(f"**Left channel** \N{WAVING HAND SIGN}")
            await self.bot.create_activity()
        await ctx.send(f"**Left channel** \N{WAVING HAND SIGN}")


def setup(bot):
    cog = Music(bot)
    bot.add_cog(cog)
