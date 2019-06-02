import discord
import youtube_dl
import asyncio
import logging

from discord.ext import commands
from sys import platform

log = logging.getLogger(__name__)

YTDL_OPTS = {
    'format': 'bestaudio/best',
    'outtmpl': '%(extractor)s-%(id)s-%(title)s.%(ext)s',
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

FFMPEG_OPTS = {
    'options': '-vn'
}

DEV_SERVER_ID = 366583119622569986

ytdl = youtube_dl.YoutubeDL(YTDL_OPTS)


# Load the libopus binaries into the bot to allow for the
# parsing of audio files.
try:
    if not discord.opus.is_loaded:
        if platform == 'linux' or 'linux2':
            discord.opus.load_opus('libopus.so')
        if platform == 'darwin':
            discord.opus.load_opus('libopus.dylib')
            
except OSError:
    log.warning("Libopus binaries could not be located.")
except Exception as e:
    log.error(e)


class YoutubeSource(discord.PCMVolumeTransformer):
    def __init__(self, source, *, data, volume=0.5):
        super().__init__(source, volume)
        self.data = data
        self.title = data.get('title')
        self.url = data.get('url')

    @classmethod
    async def from_url(cls, url, *, loop=None, stream=False):
        loop = loop or asyncio.get_event_loop()
        data = await loop.run_in_executor(None,
               lambda: ytdl.extract_info(url, download=not stream))

        if 'entries' in data:
            # Take the first index of the playlist
            data = data['entries'][0]

        filename = data['url'] if stream else ytdl.prepare_filename(data)
        return cls(discord.FFmpegPCMAudio(filename, **FFMPEG_OPTS), data=data)


class Voice(commands.Cog):
    """
    Plugin for voice channel related commands. Including:
        - Playing music
        - Playing Youtube streams
        - Controling bot volume
        - SOON: Server queues
    """

    def __init__(self, jager):
        self.jager = jager

    @commands.command()
    async def join(self, ctx, *, channel : discord.VoiceChannel):
        if ctx.voice_client is not None:
            await ctx.voice_client.move_to(channel)
            return await ctx.send(f'**MOVING TO** `{channel}` ' \
                                   '\N{MULTIPLE MUSICAL NOTES}')

        await channel.connect()
        await ctx.send(f'**CONNECTING TO** `{channel}`. '
                        '\N{MULTIPLE MUSICAL NOTES}')

    @commands.command()
    async def play(self, ctx, *, url):
        """
        This plays from mostly youtube but also any other site that
        YoutubeDL supports. Might refactor from YTDL to Lavalink
        to allow for enhanced soundcloud and spotify support.

        This updated version of the player system allows for the video
        to be streamed directly from the site instead of predownloading.
        """

        async with ctx.typing():
            player = await YoutubeSource.from_url(url, loop=self.jager.loop, stream=True)
            ctx.voice_client.play(player,
                after=lambda e: log.error('VC Err: %s' % e) if e else None)

            if ctx.guild.id == DEV_SERVER_ID:
                activity = discord.Activity(name=player.title, type=2)
                await self.bot.change_presence(activity=activity)

            fmt = '**NOW PLAYING** `{}`'
            await ctx.send(fmt.format(player.title))

    @commands.command()
    async def volume(self, ctx, volume : int):
        """
        Changes the player object's volume property.
        Uses a floating point percentage. This might fix any issues.
        """

        if ctx.voice_client is None:
            return await ctx.send('I am not connected to a voice channel. '
                                  'Use `!join` or `!play <song name> to add '
                                  'me to your channel.')
        ctx.voice_client.source.volume = volume / 100
        await ctx.send(f'Changed volume to **{volume}%**.')

    @commands.command()
    async def stop(self, ctx):
        prompt = 'This will disconnect the bot. **If you want to just stop ' \
                 'the music why not instead use** `{}pause`?. Would you ' \
                 'still like to go ahead and disconnect the bot from the ' \
                 'voice channel?'
        auth = await ctx.authorize(prompt)
        if not auth:
            return
        await ctx.voice_client.disconnect()
        if ctx.guild.id == DEV_SERVER_ID:
            await ctx.send(f'**Left channel** \N{WAVING HAND SIGN}')
            await self.bot.create_activity()
        await ctx.send(f'**Left channel** \N{WAVING HAND SIGN}')

    @commands.command()
    async def pause(self, ctx):
        if ctx.voice_client.is_playing():
            ctx.voice_client.pause()
            await ctx.send("**Paused** \N{DOUBLE VERTICAL BAR}")
        else:
            return await ctx.send('**Not playing anything.** Use '
                                  '`{}play` to play something.'
                                  ''.format(ctx.prefix))

    @commands.command()
    async def resume(self, ctx):
        if ctx.voice_client.is_paused():
            ctx.voice_client.resume()
            await ctx.send('**Resumed** \N{BLACK RIGHT-POINTING TRIANGLE}')
        else:
            return await ctx.send('**Not paused.** Use `{}pause` to pause.'
                                  ''.format(ctx.prefix))

    @play.before_invoke
    async def insure_voice_connection(self, ctx):
        if ctx.voice_client is None:
            if ctx.author.voice:
                await ctx.send(f'**CONNECTING TO** `{channel}`. '
                        '\N{MULTIPLE MUSICAL NOTES}')
                await ctx.author.voice.channel.connect()
            else:
                ctx.send('You are not connected to a voice channel '
                '\N{GRIMACING FACE}')
                raise commands.CommandError('User not connected to VC.')
        elif ctx.voice_client.is_playing:
            ctx.voice_client.stop()


def setup(bot):
    cog = Voice(bot)
    bot.add_cog(cog)
