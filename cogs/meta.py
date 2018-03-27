import discord
import traceback

from discord.ext import commands
from inspect import cleandoc
from ext.paginator import HelpPaginator, CouldNotPaginate


class Meta:
    """Info commands for the bot. Good for new users."""

    def __init__(self, bot):
        self.bot = bot
        bot.remove_command('help')

    @commands.command()
    async def streams(self, ctx):
        em = discord.Embed()
        em.set_author(name="Twitch",
                      icon_url='http://www.android-user.de/wp-content/uploads/2014/08/37515-twitch-icon.png')
        em.color = discord.Color.purple()
        desc = "Join Euab on Twitch where he streams stuff like games. " \
               "Yay.\nTo get to it either visit this link:\n" \
               "https://twitch.tv/euab \nor click The Oofster and click " \
               "watch."
        em.description = desc
        await ctx.send(embed=em)

    async def on_guild_join(self, guild):
        owner = guild.owner

        fmt = """
        **Hi**, `{username}` :wave:! Cheers for adding me to your Discord server: `{guild}` :smile:
        
        Here are some commands to get you going :thumbsup:
        `!prefix <new prefix>` - Change the server prefix, requires moderator.
        `!play <song name>` - Play a song. You need to be in a voice channel.
        `!help` - Hmm. Not really sure.
        
        **Use** `!help` **at any time to find out more about the bot's commands.**
        """
        msg = fmt.format(username=owner.name,
                         guild=guild.name)
        msg = cleandoc(msg)
        await owner.send(msg)

    @commands.command(name='help')
    async def _help(self, ctx, *, command: str = None):
        try:
            if command is None:
                pages = await HelpPaginator.from_bot(ctx)
            else:
                entity = self.bot.get_cog(command) or self.bot.get_command(command)

                if entity is not None:
                    clean = command.replace('@', '@\u200b')
                    return await ctx.send(f'Command or category **"{clean}"** not found.')
                elif isinstance(entity, commands.Command):
                    pages = await HelpPaginator.from_command(ctx, entity)
                else:
                    pages = await HelpPaginator.from_cog(ctx, entity)

            await pages.paginate()
        except Exception as e:
            traceback.print_exc()


def setup(bot):
    cog = Meta(bot)
    bot.add_cog(cog)