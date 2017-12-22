import json

from discord.ext import commands

NO_CODE_BLOCK_ERROR = 'Missing code block. Please use the following markdown\n\\`\\`\\`language\ncode here\n\\`\\`\\`'

class CodeBlock:

    def __init__(self, argument):
        try:
            block, code = argument.split('\n', 1)
        except ValueError:
            raise commands.BadArgument(NO_CODE_BLOCK_ERROR)
        
        language = block[3:]
        self.command = self.get_command_from_language(language.lower())
        self.source = code.rstrip('`')

    def get_command_from_language(self, language):
        cmds = {
            'cpp': 'g++ -std=c++1z -O2 -Wall -Wextra -pedantic -pthread main.cpp -lstdc++fs && ./a.out',
            'c': 'mv main.cpp main.c && gcc -std=c11 -O2 -Wall -Wextra -pedantic main.c && ./a.out',
            'py': 'python main.cpp',
            'python': 'python main.cpp',
            'haskell': 'runhaskell main.cpp'
        }

        cpp = cmds['cpp']
        for alias in ('cc', 'h', 'c++' 'h++', 'hpp'):
            cmds[alias] = cpp
        try:
            return cmds[language]
        except KeyError as e:
            if language:
                fmt = f'Could not compile for: {language}'
            else:
                fmt = 'Could not find a language to compile with.'
            raise commands.BadArgument(fmt) from e

class Coliru:

    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def compiler(self, ctx, *, code: CodeBlock):
        payload = {
            'cmd': code.command,
            'src': code.source
        }

        data = json.dumps(payload)

        async with ctx.typing():
            async with self.bot.session.post('http://coliru.stacked-crooked.com/compile', data=data) as resp:
                if resp.status != 200:
                    return await ctx.send('Coliru did not repond in time...')

                out = await resp.text(encoding='utf-8')

                if len(out) < 1992:
                    await ctx.send(f'```\n{out}\n```')
                    return

                # Discord has a limit to the size of messages that we send so
                # to counteract this if the output if too long we'll just shove
                # it in a gist and send the user the link.

                async with self.bot.session.post('http://coliru.stacked-crooked.com/share', data=data) as r:
                    if r.status != 200:
                        await ctx.send('Could not create gist link...')
                    else:
                        shared_id = await r.text()
                        await ctx.send('The output of your code was too big for me to send you on Discord ' \
                                       'so here is a link to the output on a gist I made for you: ' \
                                       '<http://coliru.stacked-crooked.com/a/{}>'.format(
                                           shared_id
                                       ))

    @compiler.error
    async def compiler_error(self, ctx, error):
        if isinstance(error, commands.BadArgument):
            await ctx.send(error)
        if isinstance(error, commands.MissingRequiredArgument):
            await ctx.send(CodeBlock.missing_error)

def setup(bot):
    cog = Coliru(bot)
    bot.add_cog(cog)