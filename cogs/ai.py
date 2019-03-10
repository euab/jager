# This file is only using an API for now. Soon I want to make my own AI implementation.

import discord
import asyncio
import requests
import json
import secrets

from discord.ext import commands

user = secrets.CLEVERBOT_API_USER
key  = secrets.CLEVERBOT_API_KEY


class AI(commands.Cog):
    """
    Simple class using an API to send and retrieve an automated conversation.

    Soon to be upgraded with a self made AI.
    """

    def __init__(self, jager):
        self.jager = jager

    async def on_message(self, message):
        if not message.author.bot and (message.guild is None or self.jager.user in message.mentions):
            async with message.channel.typing(): #self.jager.user.send_typing(message.channel):
                txt = message.content.replace(message.guild.me.mention, '') if message.guild else message.content
                resp = json.loads(requests.post('https://cleverbot.io/1.0/ask', json={
                    'user': user,
                    'key': key,
                    'nick': 'jager',
                    'text': txt
                }).text)

                if resp['status'] == 'success':
                    try:
                        await message.channel.send(resp['response'])
                    except Exception as e:
                        print(e)


def setup(bot):
    cog = AI(bot)
    requests.post('https://cleverbot.io/1.0/create', json={'user':user, 'key':key, 'nick':'jager'})
    bot.add_cog(cog)
