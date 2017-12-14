import discord
import json
import io

from discord.ext import commands
from colorthief import ColorThief
from urllib.parse import urlparse

class LeContext(commands.Context):

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
		IMAGE_TYPES = ['.png', '.jpg', '.jpeg', '.webp', '.gif']
		parsed = urlparse(url)
		if any(parsed.path.endswith(i) for i in IMAGE_TYPES):
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

	def load_json(path=None):
		with open(path) as f:
			return json.load(f)

	def save_json(data, path=None):
		with open(path, 'w') as f:
			f.write(json.dumps(data, indent=4))
