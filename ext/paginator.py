import discord
import asyncio
import logging
import itertools
import inspect
import re

from .embeds import random_color

log = logging.getLogger(__name__)

MENTION_REGEX = re.compile(r'<@\!?([0-9]{1,19})>')


class CouldNotPaginate(Exception):
    pass


class Pages:
    """
    A paginator that users can use to navigate between pages
    of information
    """

    def __init__(self, ctx, *, entries, per_page=12, show_entry_count=True):
        self.bot = ctx.bot
        self.message = ctx.message
        self.channel = ctx.channel
        self.author = ctx.author
        self.entries = entries
        self.per_page = per_page
        pages, left_over = divmod(len(self.entries), self.per_page)
        if left_over:
            pages += 1
        self.maximum_pages = pages
        self.embed = discord.Embed(colour=random_color())
        self.paginating = len(entries) > per_page
        self.show_entry_count = show_entry_count
        self.reaction_emojis = [
            ('\N{BLACK LEFT-POINTING DOUBLE TRIANGLE WITH VERTICAL BAR}', self.first_page),
            ('\N{BLACK LEFT-POINTING TRIANGLE}', self.previous_page),
            ('\N{BLACK RIGHT-POINTING TRIANGLE}', self.next_page),
            ('\N{BLACK RIGHT-POINTING DOUBLE TRIANGLE WITH VERTICAL BAR}', self.last_page),
            ('\N{INPUT SYMBOL FOR NUMBERS}', self.numbered_page ),
            ('\N{BLACK SQUARE FOR STOP}', self.kill_pages),
            ('\N{INFORMATION SOURCE}', self.show_help)
        ]

        if ctx.guild is not None:
            self.permissions = self.channel.permissions_for(ctx.guild.me)
        else:
            self.permissions = self.channel.permissions_for(ctx.bot.user)

        if not self.permissions.embed_links:
            raise CouldNotPaginate("Bot does not have permission to embed links.")

        if not self.permissions.send_messages:
            raise CouldNotPaginate("Bot is not able to send messages")

        if self.paginating:
            if not self.permissions.add_reactions:
                raise CouldNotPaginate('Bot does not have permissions to add reactions.')
            
            if not self.permissions.read_message_history:
                raise CouldNotPaginate('Bot does not have permission to read message history.')

    def get_page(self, page):
        base = (page - 1) * self.per_page
        return self.entries[base:base + self.per_page]

    async def show_page(self, page, *, first=False):
        self.current_page = page
        entires = self.get_page(page)
        p = []
        for index, entry in enumerate(entries, 1 + ((page - 1) * self.per_page)):
            p.append(f'{index}. {entry}')

        if self.maximum_pages > 1:
            text = f'Page {page}/{maximum_pages} ({len(self.entries)} entries)'
        else:
            text = f'Page {page}/{self.maximum_pages}'

        self.embed.set_footer(text=text)

        if not self.paginating:
            self.embed.description = '\n'.join(p)

        if not first:
            self.embed.description = '\n'.join(p)
            await self.message.edit(embed=self.embed)
            return

        p.append('')
        p.append('Still not sure? You can find out more by reacting to this message with \N{INFORMATION SOURCE}')
        self.embed.description = '\n'.join(p)
        self.message = await self.channel.send(embed=self.embed)
        for reaction, _ in self.reaction_emojis:
            if self.maximum_pages == 2 and reaction in ('\u23ed', '\u23ee'):
                continue

            await self.message.add(reaction)

    async def check_show_page(self, page):
        if page != 0 and page <= self.maximum_pages:
            await self.show_page(page)

    async def first_page(self):
        await self.show_page(1)

    async def last_page(self):
        await self.show_page(self.maximum_pages)

    async def next_page(self):
        await self.check_show_page(self.current_page + 1)

    async def previous_page(self):
        await self.check_show_page(self.current_page - 1)

    async def show_current_page(self):
        if self.paginating:
            await self.show_page(self.current_page)

    async def numbered_page(self):
        to_delete = []
        to_delete.append(await self.channel.send('What page would you like to go to?'))

        def check(m):
            return m.author == self.author and \
                self.channel == m.channel and \
                m.content.isdigit()

        try:
            msg = await self.bot.wait_for('message', check=check, timeout=30.0)
        except asyncio.TimeoutError:
            to_delete.append(await self.channel.send('Had to wait for too long.'))
            await asyncio.sleep(5)
        else:
            page = int(msg.content)
            to_delete.append(msg)
            if page != 0 and page <= self.maximum_pages:
                await self.show_page(page)
            else:
                to_delete.append(await self.channel.send(f'An incorrect page has been given. ({page}/{self.maximum_pages})'))
                await asyncio.sleep(5)

        try:
            await self.channel.delete_messages(to_delete)
        except Exception as e:
            log.error(e)

    async def show_help(self):
        messages = ["Hi, welcome to the new paginator!\n"]
        messages.append('This allows you to move through different pages of text just be reacting to this message. '
                        'The reactions that you can react with are as follows.\n')
        
        for emoji, func in self.reaction_emojis:
            messages.append(f'{emoji} {func.__doc__}')

        self.embed.description = '\n'.join(messages)
        self.embed.clear_fields()
        self.embed.set_footer(text=f'We were on page {self.current_page} before this message')
        await self.message.edit(embed=self.embed)

        async def revert_to_current_page():
            await asyncio.sleep(60.0)
            await self.show_current_page()

        self.bot.loop.create_task(revert_to_current_page())

    async def kill_pages(self):
        await self.message.delete()
        self.paginating = False

    def check_user_reactions(self, reaction, user):
        if user is None or user.id != self.author.id:
            return False

        if reaction.message.id != self.message.id:
            return False

        for emoji, func in self.reaction_emojis:
            if reaction.emoji == emoji:
                self.match = func
                return True
        return False

    async def paginate(self):
        first_page = self.show_page(1, first=True)
        if not self.paginating:
            await first_page()
        else:
            self.bot.loop.create_task(first_page)

        while self.paginating:
            try:
                reaction, user = await self.bot.wait_for('reaction_add', check=self.check_user_reactions,
                                                         timeout=120.0)
            except asyncio.TimeoutError:
                self.paginating = False
                try:
                    await self.message.clear_reactions()
                except Exception as e:
                    log.warning(e)
                finally:
                    break

            try:
                await self.message.remove_reaction(reaction, user)
            except Exception as e:
                log.warning(e)

            await self.match()

        
class HelpPaginator(Pages):
    def __init__(self, ctx, entries, *, per_page=4):
        super().__init__(ctx, entries=entries, per_page=per_page)
        self.reaction_emojis.append(('\N{WHITE QUESTION MARK ORNAMENT}', self.show_bot_help))
        self.total = len(entries)

    @classmethod
    async def from_cog(cls, ctx, cog):
        cog_name = cog.__class__.__name__
        entries = sorted(ctx.bot.get_cog_commands(cog_name), key=lambda c: c.name)
        entries = [cmd for cmd in entries if (await _can_run(cmd, ctx)) and not cmd.hidden]
        self = cls(ctx, entries)
        self.title = f'{cog_name} Commands'
        self.description = inspect.getdoc(cog)
        self.prefix = cleanup_prefix(ctx.bot, ctx.prefix)

        #ctx.release()
        return self

    @classmethod
    async def from_command(cls, ctx, command):
        try:
            entries = sorted(command.commands, key=lambda c: c.name)
        except AttributeError:
            entries = []
        else:
            entries = [cmd for cmd in entries if (await _can_run(cmd, ctx)) and not cmd.hidden]

        self = cls(ctx, entries)
        self.title = command.signature

        if command.description:
            self.description = f'{command.description}\n\n{command.help}'
        else:
            self.description = command.help or 'No help given, Sorry!'

        self.prefix = cleanup_prefix(ctx.bot, ctx.prefix)

        #await ctx.release()
        return self

    @classmethod
    async def from_bot(cls, ctx):
        def key(c):
            return c.cog_name or '\u200bMisc'

        entries = sorted(ctx.bot.commands, key=key)
        nested_pages = []
        per_page = 9

        for cog, commands in itertools.groupby(entries, key=key):
            plausible = [cmd for cmd in commands if (await _can_run(cmd, ctx)) and not cmd.hidden]
            if len(plausible) == 0:
                continue
            
            description = ctx.bot.get_cog(cog)
            if description is None:
                description = discord.Embed.Empty()
            else:
                description = inspect.getdoc(description) or discord.Embed.Empty

            nested_pages.extend((cog, description, plausible[i:i + per_page]) for i in range(0, len(plausible), per_page))

        self = cls(ctx, nested_pages, per_page=1)
        self.prefix = cleanup_prefix(ctx.bot, ctx.prefix)
        #await ctx.release()

        self.get_page = self.get_bot_page
        self._is_bot = True

        self.total = sum(len(o) for _, _, o in nested_pages)
        return self

    def get_bot_page(self, page):
        cog, description, commands = self.entries[page - 1]
        self.title = f'{cog} Command'
        self.description = description
        return commands

    async def show_page(self, page, *, first=False):
        self.current_page = page
        entries = self.get_page(page)

        self.embed.clear_fields()
        self.embed.description = self.description
        self.embed.title = self.title

        if hasattr(self, '_is_bot'):
            value = 'For more help contact Euab#3685'
            self.embed.add_field(name='Support', value=value, inline=False)

        self.embed.set_footer(text=f'Use "{self.prefix}help command" for more help on a command')

        signature = _command_signature

        for entry in entries:
            self.embed.add_field(name=signature(entry), value=entry.short_doc or "No help given", inline=False)

        if self.maximum_pages:
            self.embed.set_author(name=f'Page {page}/{self.maximum_pages} ({self.total} commands)')

        if not self.paginating:
            return await self.channel.send(embed=self.embed)

        if not first:
            return await self.message.edit(embed=self.embed)

        self.message = await self.channel.send(embed=self.embed)
        for reaction, _ in self.reaction_emojis:
            if self.maximum_pages == 2 and reaction in ('\u23ed', '\u23ee'):
                continue

            await self.message.add_reaction(reaction)

    async def show_help(self):
        self.embed_title = 'How to use the paginator'
        self.embed.description = 'Hi! Welcome to the help page.'

        messages = [f'{emoji} {func.__doc__}' for emoji, func in self.reaction_emojis]
        self.embed.clear_fields()
        self.embed.add_field(name='What are these reactions for?', value='\n'.join(messages), inline=True)

        self.embed.set_footer(text=f'We were on page {self.current_page} before this message')
        await self.message.edit(embed=self.embed)

        async def revert_to_current_page():
            await asyncio.sleep(30.0)
            await self.show_current_page()

        self.bot.loop.create_task(revert_to_current_page())

    async def show_bot_help(self):
        self.embed.title = 'How to use the bot'
        self.embed.description = 'Hello! Welcome to the help page.'
        self.embed.clear_fields()

        entries = (
            ('<argument>', 'This means that the argument is __**compulsory**__.'),
            ('[argument]', 'This means that the argument is __**optional**__.'),
            ('[A|B]', 'This means that it can be __**either A or B**__'),
            ('[argument...]', 'This means that you can help multiple arguments.\n' \
                'Now that you know how to use the bot, you should be good! One last ' \
                'thing...\n__**don\'t type in the brackets')
        )

        self.embed.add_field(name='How the heck does this thing work?', value='Son it\'s pretty simple.')

        for name, value in entries:
            self.embed.add_field(name=name, value=value, inline=False)

        self.embed.set_footer(text=f'We were on page {self.current_page} before this message.')
        await self.message.edit(embed=self.embed)

        async def revert_to_current_page():
            await asyncio.sleep(30.0)
            await self.show_current_page()

        self.bot.loop.create_task(revert_to_current_page)


async def _can_run(cmd, ctx):
    try:
        return await cmd.can_run(ctx)
    except:
        return False

def cleanup_prefix(bot, prefix):
    m = MENTION_REGEX.match(prefix)
    if m:
        user = bot.get_user(int(m.group(1)))
        if user:
            return f'@{user.name} '
    return prefix

def _command_signature(cmd):
    result = [cmd.qualified_name]
    if cmd.usage:
        result.append(cmd.usage)
        return ' '.join(result)

    params = cmd.clean_params
    if not params:
        return ' '.join(result)

    for name, param in params.items():
        if param.default is not param.empty:
            should_print = param.default if isinstance(param.default, str) else param.default is not None
            if should_print:
                result.append(f'[{name}={param.default!r}]')
            else:
                result.append(f'[{name}]')