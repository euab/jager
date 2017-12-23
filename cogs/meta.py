import discord

from discord.ext import commands


class Meta:
    """Info commands for the bot. Good for new users."""

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def streams(self, ctx):
        em = discord.Embed()
        em.set_author(name="Twitch",
                      icon_url='http://www.android-user.de/wp-content/uploads/2014/08/37515-twitch-icon.png')
        em.color = discord.Color.purple()
        desc = "Join Euab on Twitch where he streams stuff like games. " \
               "Yay.\nTo get to it either visit this link:\n" \
               "https://twitch.tv/euab \nor click RickBot and click " \
               "watch."
        em.description = desc
        await ctx.send(embed=em)


def setup(bot):
    cog = Meta(bot)
    bot.add_cog(cog)