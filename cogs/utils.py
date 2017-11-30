import discord
import datetime
import psutil
import asyncio
import aiohttp
import traceback
import time
import json
import sys
import os
import re
import inspect
import io
import textwrap

from discord.ext import commands
from ext import embeds
from ext.paginator import PaginatorSession

class Utils:

    def __init__(self, bot):
        self.bot = bot

    @commands.command(hidden=True)
    @commands.is_owner()
    async def maintenance(self, ctx):
        if self.bot.maintenance_mode is True:
            await self.bot.change_presence(
                status=discord.Status.online,
                game=None
            )

            self.bot.maintenance_mode = False

            await ctx.send('Maintenance mode off')

        else:
            await self.bot.change_presence(
                status=discord.Status.dnd,
                game=discord.Game(name='Maintenance...')
            )

            self.bot.maintenance_mode = True

            await ctx.send('Maintenance mode on')

    @commands.command()
    async def invite(self, ctx):
        perms = discord.Permissions.none()
        perms.read_messages = True
        perms.external_emojis = True
        perms.send_messages = True
        perms.attach_files = True
        perms.embed_links = True
        perms.manage_messages = True
        perms.add_reactions = True
        resp = f'{discord.utils.oauth_url(self.bot.user.id, perms)}'
        await ctx.send(resp)

    @commands.command(name='bot', aliases=['about', 'info', 'status'])
    async def _bot(self, ctx):
        em = discord.Embed()
        em.timestamp = datetime.datetime.utcnow()
        status = str(ctx.guild.me.status)
        if status == 'online':
            em.set_author(name="Bot Information", icon_url='https://i.imgur.com/YpQyZO7.png')
            em.color = discord.Color.green()
        elif status == 'dnd':
            status = 'maintenance'
            em.set_author(name="Bot Information", icon_url=None)
            em.color = discord.Color.purple()
        else:
            em.set_author(name="Bot Information", icon_url='https://i.imgur.com/YpQyZO7.png')
            em.color = discord.Color.red()

        total_online = len({m.id for m in self.bot.get_all_members() if m.status is not discord.Status.offline})
        total_unique = len(self.bot.users)
        channels = sum(1 for g in self.bot.guilds for _ in g.channels)

        now = datetime.datetime.utcnow()
        delta = now - self.bot.uptime
        hours, remainder = divmod(int(delta.total_seconds()), 3600)
        minutes, seconds = divmod(remainder, 60)
        days, hours = divmod(hours, 24)

        fmt = '{h}h {m}m {s}s'
        if days:
            fmt = '{d}d' + fmt
        uptime = fmt.format(d=days, h=hours, m=minutes, s=seconds)

        em.add_field(name='Current Status', value=str(status).title())
        em.add_field(name='Uptime', value=uptime)
        em.add_field(name='Latency', value=f'{self.bot.latency*1000:.2f} ms')
        em.add_field(name='Guilds', value=len(self.bot.guilds))
        em.add_field(name='Members', value=f'{total_online}/{total_unique} online')
        em.add_field(name='Channels', value=f'{channels} total')
        memory_usage = self.bot.process.memory_full_info().uss / 1024**2
        cpu_usage = self.bot.process.cpu_percent() / psutil.cpu_count()
        em.add_field(name='RAM Usage', value=f'{memory_usage:.2f} MiB')
        em.add_field(name='CPU Usage', value=f'{cpu_usage:.2f}% CPU Usage')
        em.add_field(name='Commands Run', value=sum(self.bot.commands_used.values()))
        em.add_field(name='GitHub', value='[Click Here](https://github.com/rickbotdiscord/rickbot)')
        em.set_footer(text=f'Your shard: {self.bot.shard_id}')

        await ctx.send(embed=em)

    @commands.command()
    @commands.has_permissions(manage_guild=True)
    async def prefix(self, ctx, *, prefix):
        id = str(ctx.guild.id)
        conf = ctx.load_json('data/guild.json')
        conf[id] = prefix
        ctx.save_json(conf, 'data/guild.json')
        await ctx.send(f'I changed your prefix to: `{prefix}`')

    @commands.command(hidden=True)
    @commands.is_owner()
    async def update(self, ctx):
        em = discord.Embed()
        em.title = 'Updating bot'
        em.description = 'Pulling from repository and restarting shards...'
        await ctx.send(embed=em)
        command = 'sh update.sh'
        os.system(command)
        exit(0)

    def format_cog_help(self, name, cog, prefix):
        sigs = []

        for cmd in self.bot.commands:
            if cmd.hidden:
                continue
            if cmd.instance is cog:
                sigs.append(len(cmd.qualified_name) + len(prefix))
                if hasattr(cmd, 'all_commands'):
                    for c in cmd.all_commands.values():
                        sigs.append(len('\u200b └─ ' + c.name) + 1)
        maxlen = max(sigs)

        fmt = ''
        for cmd in self.bot.commands:
            if cmd.instance is cog:
                if cmd.hidden:
                    continue
                fmt += f'`{prefix+cmd.qualified_name:<{maxlen}} `'
                fmt += f'{cmd.short_doc:<{maxlen}}`\n'
                if hasattr(cmd, 'commands'):
                    for c in cmd.commands:
                        branch = '\u200b └─ ' + c.name
                        fmt += f"`{branch:<{maxlen + 1}} "
                        fmt += f"{c.short_doc:<{maxlen}}`\n"

        em.discord.Embed(title=name.replace('_', ' '))
        em.color = embeds.random_color()
        em.description = '*'+(inspect.getdoc(cog))+'*'
        em.add_field(name='Commands', value=fmt)
        em.set_foot(text=f'Type {prefix}help command for more info on a command.')

        return em

    def format_command_help(self, command, prefix):
        name = command.replace(' ', '_')
        cog = self.bot.cogs.get(name)
        if cog is not None:
            return self.format_cog_help(name, cog, prefix)
        cmd = self.bot.get_command(command)
        if cmd is not None:
            return discord.Embed(
                color=embeds.random_color(),
                title=f'`{prefix}{cmd.signature}`',
                description=cmd.help
                )

    @commands.command()
    async def bothelp(self, ctx, *, command=None):
        prefix = (await self.bot.get_prefix(ctx.message))[2]

        if command:
            em = self.format_command_help(command, prefix)
            if em:
                return await ctx.send(embed=em)
            else:
                return await ctx.send('Could not find a cog or command by that name.')

        pages = []

        for name, cog in sorted(self.bot.cogs.items()):
            em = self.format_cog_help(name, cog, prefix)
            pages.append(em)

        p_session = PaginatorSession(ctx,
            footer_text=f'Type {prefix}help command for more info on a command.',
            pages=pages
            )

        await p_session.run()

def setup(bot):
    cog = Utils(bot)
    bot.add_cog(cog)
