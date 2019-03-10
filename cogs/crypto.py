import blockchain
import discord

from datetime import datetime
from discord.ext import commands


class Crypto(commands.Cog):
    """
    Interact with the Blockchain wallet API to see exchange
    rates between cryptocurrencies and fiat currencies.
    """

    def __init__(self, bot):
        self.bot = bot

    @commands.group()
    async def bitcoin(self, ctx):
        """Get information on bitcoin"""
        if ctx.invoked_subcommand is None:
            return await ctx.send("You need to use a subcommand "
                                  "eg:\n`{}bitcoin stats`".format(
                                      ctx.prefix
                                  ))
    
    @bitcoin.command()
    async def stats(self, ctx):
        stats = blockchain.statistics.get()
        em = discord.Embed()
        em.set_author(name="Bitcoin Information", icon_url="https://bitcoin.org/img/icons/opengraph.png")
        em.timestamp = datetime.utcnow()
        em.color = discord.Color.orange()
        em.add_field(name="Trade Volume (USD)", value=str(stats.trade_volume_usd) + " USD")
        em.add_field(name="Trade Volume (BTC)", value=str(stats.trade_volume_btc) + " BTC")
        em.add_field(name="BTC Market Price USD", value=str(stats.market_price_usd) + " USD")
        em.add_field(name="Miners' Revenue (USD)", value=str(stats.miners_revenue_usd) + " USD")
        em.add_field(name="Miners' Revenue (BTC)", value=str(stats.miners_revenue_btc) + " BTC")
        em.add_field(name="BTC Mined", value=str(stats.btc_mined) + " BTC")
        em.add_field(name="Total Blocks", value=str(stats.total_blocks))
        em.add_field(name="Total BTC", value=str(stats.total_btc) + " BTC")
        em.add_field(name="Difficulty", value=str(stats.difficulty))
        em.add_field(name="Hash Rate", value=str(stats.hash_rate) + "H")
        em.add_field(name="Mined Blocks", value=str(stats.mined_blocks))
        em.add_field(name="Minutes Between Blocks", value=str(stats.minutes_between_blocks) + "m")
        em.add_field(name="Estimated BTC Sent", value=str(stats.estimated_btc_sent) + " BTC")
        em.add_field(name="Total BTC Sent", value=str(stats.total_btc_sent) + " BTC")
        em.add_field(name="Total BTC Fees", value=str(stats.total_fees_btc) + " BTC")
        em.set_footer(text="https://blockchain.info/api")

        await ctx.send(embed=em)


def setup(bot):
    cog = Crypto(bot)
    bot.add_cog(cog)
        
