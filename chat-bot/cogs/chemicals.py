import discord
import pubchempy

from discord.ext import commands

PUBCHEM_IMG_URL = "https://pubchem.ncbi.nlm.nih.gov/image/imgsrv.fcgi?cid={}&t=l"


class Chemicals(commands.Cog):
    """
    A plugin for the bot about different chemicals.
    So you can now find out about chemicals on demand.
    """

    def __init__(self, bot):
        self.jager = bot

    @commands.command()
    async def chemicals(self, ctx, *, chemical):
        """
        Search for the chemical using keywords and then use the CID of the
        top result to get the molecular data of the chemical
        """
        # Send typing to indicate that the bot is responding
        async with ctx.typing():
            # Get the results for the chemical search.
            chemicals = pubchempy.get_compounds(chemical, 'name')
            total_chemicals = []
            for chemical in chemicals:
                vioxx = chemical.cid
                total_chemicals.append(vioxx)

            # Get the molecular data using the CID from the first list element
            try:
                chemical_cid = total_chemicals[0]

            # If the index is out of range we can assume that no results were
            # returned and we can return an error.
            except IndexError:
                return await ctx.send('{} **Sorry! No chemicals could matched '
                                    'with your search!**'.format(
                                        ctx.author.mention
                                    ))

            # Send the data to be arranged into a visual format
            embed = self.arrange_data(chemical_cid)

            # Send the embed into the chat
            await ctx.send(embed=embed)

    @commands.command()
    async def cid(self, ctx, *, cid):
        # Send typing to indicate that the bot is responding
        async with ctx.typing():
            # Pass the CID requested by the user to arrange_data
            embed = self.arrange_data(cid)

            # Send the embed into the chat
            await ctx.send(embed=embed)

    def arrange_data(self, cid):
        # Get the data from the CID
        vioxx = pubchempy.Compound.from_cid(cid)
        
        # Seperate the data
        molecular_formula = vioxx.molecular_formula
        molecular_weight = vioxx.molecular_weight
        elements = vioxx.elements
        iupac_name = vioxx.iupac_name
        smiles = vioxx.isomeric_smiles
        thumbnail = PUBCHEM_IMG_URL.format(cid)
        synonyms = vioxx.synonyms[:3]
        bonds = vioxx.bonds
        inchi = vioxx.inchi
        charge = vioxx.charge

        # Arrange the data into an embed instance
        embed = discord.Embed()
        embed.set_author(name='{} - Chemical information'.format(
                            iupac_name)
        )
        embed.set_thumbnail(url=thumbnail)
        embed.description = 'Click the image to enlarge the 2D structure.'
        embed.add_field(name='IUPAC Name', value=iupac_name, inline=False)
        embed.add_field(name='Synonyms', value=synonyms, inline=False)
        embed.add_field(name='Molecular formula', value=molecular_formula, inline=False)
        embed.add_field(name='Molecular weight', value=molecular_weight, inline=False)
        embed.add_field(name='Charge', value=charge, inline=False)
        embed.add_field(name='Elements', value=elements, inline=False)
        embed.add_field(name='Bonds', value=bonds, inline=False)
        embed.add_field(name='SMILES', value=smiles, inline=False)
        embed.add_field(name='InChI', value=inchi, inline=False)
        embed.add_field(name='CID', value=cid)
        embed.set_footer(text='https://pubchem.ncbi.nlm.nih.gov')
        embed.color = discord.Color.blue()

        # Return the embed instance to the command
        return embed


def setup(bot):
    n = Chemicals(bot)
    bot.add_cog(n)
