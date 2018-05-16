import discord
import json
import io

from discord.ext import commands
from colorthief import ColorThief
from urllib.parse import urlparse


class LeContext(commands.Context):
    """
    Provides the bot with context for each message sent.
    """

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

    @property
    def session(self):
        return self.bot.session

    def delete(self):
        # A cure for my laziness.
        return self.message.delete()

    async def purge(self, *args, **kwargs):
        return await self.channel.purge(*args, **kwargs)

    @staticmethod
    def valid_image_url(url):
        image_types = ['.png', '.jpg', '.jpeg', '.webp', '.gif']
        parsed = urlparse(url)
        if any(parsed.path.endswith(i) for i in image_types):
            return url.replace(parsed.query, 'size=128')
        return False

    async def get_main_colour(self, url=None, quality=10):
        avatar = self.author.avatar_url
        url = self.valid_image_url(url or avatar)

        if not url:
            raise ValueError('invalid url was passed')

        try:
            async with self.session.get(url) as resp:
                image = await resp.read()

        except Exception:
            return discord.Color.default()

        with io.BytesIO(image) as f:
            try:
                color = ColorThief(f).get_color(quality=quality)
            except Exception:
                return discord.Color.dark_grey()

        return discord.Color.from_rgb(*color)

    async def authorize(self, message, *, timeout=60.0, delete_after=True,
                        author_id=None):
        """Prompt the user to confirm an action"""
        if not self.channel.permissions_for(self.me).add_reactions:
            raise RuntimeError("Bot does not have permissions to add reactions")

        fmt = f'{message}\n**React with \N{WHITE HEAVY CHECK MARK} to accept and \N{CROSS MARK} to reject**'

        author_id = author_id or self.author.id
        msg = await self.send(fmt)

        user_confirmation = None

        def check(emoji, message_id, channel_id, user_id):
            # nonlocal: get user_confirmation from the scope of the outer function
            nonlocal user_confirmation

            if message_id != msg.id or user_id != author_id:
                return False

            utf = str(emoji)

            if utf == '\N{WHITE HEAVY CHECK MARK}':
                user_confirmation = True
                return True
            elif utf == '\N{CROSS MARK}':
                user_confirmation = False
                return True

            return False

        for emoji in ('\N{WHITE HEAVY CHECK MARK}', '\N{CROSS MARK}'):
            await msg.add_reaction(emoji)

        try:
            await self.bot.wait_for('raw_reaction_add', check=check, timeout=timeout)
        except asyncio.timeout:
            user_confirmation = None

        try:
            if delete_after:
                await msg.delete()
        except Exception as e:
            raise RuntimeError(e)
        finally:
            return user_confirmation