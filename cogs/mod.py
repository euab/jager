import discord

from discord.ext import commands


class Reason(commands.Converter):
    async def convert(self, ctx, argument):
        resp = f'{ctx.author} (ID: {ctx.author.id}): {argument}'

        if len(resp) > 512:
            limit = 512 - len(ret) - len(argument)
            raise commands.BadArgument(f'Reason is too long ({len(argument)}/{limit})')
        return resp


class MemberID(commands.Converter):
    async def convert(self, ctx, argument):
        try:
            m = await commands.MemberConverter().convert(ctx, argument)
        except commands.BadArgument:
            try:
                return int(argument, base=10)
            except ValueError:
                raise commands.BadArgument(f"{argument} is not a valid member or member ID.") from None
        else:
            can_execute = ctx.author.id == ctx.bot.owner_id or \
                          ctx.author == ctx.guild.owner or \
                          ctx.author.top_role > m.top_role

            if not can_execute:
                raise commands.BadArgument('You cannot do this action on this user due to role hierarchy.')
            return m.id


class Mod:
    """
    Moderation features for your server.
    These commands tend to be a tad more complicated.
    """

    def __init__(self, bot):
        self.bot = bot

    async def __error(self, ctx, error):
        if isinstance(error, commands.BadArgument):
            await ctx.send(error)
        elif isinstance(error, commands.CommandInvokeError):
            original = error.original
            if isinstance(original, discord.Forbidden):
                await ctx.send('**I don\'t have permissions to do this...**')
            elif isinstance(original, discord.NotFound):
                await ctx.send(f'**404 - Not found:** {original.text}')
            elif isinstance(original, discord.HTTPException):
                await ctx.send('**An error has occured.** Try again later maybe?')

    @commands.command()
    @commands.guild_only()
    @commands.has_permissions(manage_guild=True)
    async def clear(self, ctx, *, value: int):
        await ctx.send("Gettin' ready to purge")
        clear = await ctx.channel.purge(limit=value,
                                        check=None)

        amount = len(clear)
        await ctx.send(f"`Cleared {amount} messages`", delete_after=5)

    @commands.command()
    @commands.guild_only()
    @commands.has_permissions(kick_members=True)
    async def kick(self, ctx, member: discord.Member, reason: Reason = None):
        if reason is None:
            reason = f'Kicked by {ctx.author} (ID: {ctx.author.id})'
        
        await member.kick(reason=reason)
        await ctx.send(f'**{member}** has been kicked \N{OK HAND SIGN}')

    @commands.command()
    @commands.guild_only()
    @commands.has_permissions()
    async def ban(self, ctx, member: MemberID, *, reason: ActionReason = None):
        if reason is None:
            reason = f'Action done by {ctx.author} (ID: {ctx.author.id})'

        await ctx.guild.ban(discord.Object(id=member), reason=reason)
        await ctx.send("***THICC BAN HAMMER*** \N{OK HAND SIGN}")


def setup(bot):
    cog = Mod(bot)
    bot.add_cog(cog)
