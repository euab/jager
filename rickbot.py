import discord
import datetime
import traceback
import aiohttp
import psutil
import json
import sys
import os
import inspect
import logging
import secrets

from collections import defaultdict
from ext.context import LeContext
from discord.ext import commands
from datadog import DDAgent
from ext.dataIO import dataIO

log = logging.getLogger('discord')

class RickBot(commands.AutoShardedBot):

	developers = [
		'337333673781100545'
		]

	def __init__(self):
		super().__init__(command_prefix=None)
		self.session = aiohttp.ClientSession(loop=self.loop)
		self.uptime = datetime.datetime.utcnow()
		self.commands_used = defaultdict(int)
		self.process = psutil.Process()
		self.messages_sent = 0
		self.load_extensions()
		self._add_commands()
		self.remove_command('help')
		self.dd_agent_url = os.getenv("DD_AGENT_URL") or secrets.DD_AGENT_URL
		self.stats = DDAgent(self.dd_agent_url)

	def _add_commands(self):
		'''Adds commands automatically'''
		for name, attr in inspect.getmembers(self):
			if isinstance(attr, commands.Command):
				self.add_command(attr)


	def load_extensions(self, cogs=None, path='cogs.'):
		base_extensions = [x.replace('.py', '') for x in os.listdir('cogs') if x.endswith('.py')]
		for extension in cogs or base_extensions:
			try:
				self.load_extension(f'{path}{extension}')
				log.info(f'Loaded extension {extension}')

			except Exception as e:
				log.info(f'Unable to load extension {extension}. Err: {e}')
				traceback.print_exc()

	@property
	def token(self):
		os.getenv("TOKEN") or secrets.TOKEN

	@classmethod
	def init(bot, token=None):
		bot = RickBot()
		token = token or bot.token
		try:
			bot.run(token.strip('"'), bot=True, reconnect=True)
		except Exception as e:
			log.critical("COULD NOT ACCESS TOKEN!")
			log.critical(e)

	def restart(self):
		os.execv(sys.executable, ["python"] + sys.argv)

	async def get_prefix(self, message):
		return '!'

	async def on_connect(self):
		log.info("Connected to gateway")

	async def on_shard_connect(self, shard_id):
		log.info(f"Shard: {shard_id} connected... ")

	async def on_ready(self):
		log.info("Ready. Yay.")

	async def on_command(self, ctx):
		cmd = ctx.command.qualified_name.replace(' ', '_')
		log.info("Invoked: {}@{} >> {}".format(
			ctx.message.author,
			ctx.guild.name,
			ctx.command.qualified_name
		))
		self.commands_used[cmd] += 1
		self.stats.incr('rickbot.commands_invoked')

	async def on_command_error(self, ctx, error):
		log.error('Error processing command')
		log.error(error)

	async def process_commands(self, message):
		ctx = await self.get_context(message, cls=LeContext) # Le Meme
		if ctx.command is None:
			return

		await self.invoke(ctx)
		log.info('Command invoked')

	async def on_message(self, message):
		# Actual listener
		self.messages_sent += 1
		if message.author.bot:
			return
		await self.process_commands(message)

	@commands.command()
	async def ping(self, ctx):
		em = discord.Embed()
		em.title = 'Pong! Here is the latency:'
		em.description = f'{self.latency * 1000:.4f} ms'
		em.color = await ctx.get_main_colour(self.user.avatar_url)
		try:
			await ctx.send(embed=em)
		except discord.Forbidden:
			await ctx.send(em.title + em.description)

def check_folders():
    if not os.path.exists("data"):
        log.info("Creating data folder")
        os.mkdir("data")


def check_files():
    fp = "data/guild.json"
    if not dataIO.is_valid_json(fp):
        log.info("Creating guild.json")
        dataIO.save_json(fp, {})
	
def main():
	check_folders()
	check_files()
	token = os.getenv("TOKEN") or secrets.TOKEN
	debug = os.getenv("RICKBOT_DEBUG") or secrets.RICKBOT_DEBUG
	if debug:
		logging.basicConfig(level='DEBUG')
	else:
		logging.basicConfig(level='INFO')
	RickBot.init(token)

if __name__ == '__main__':	
	main()
