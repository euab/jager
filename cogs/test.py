class Test:
    """
    This test asserts the sanity of the known universe.
    That 1 + 1 == 2. If this test fails then we are all
    proably dead.
    """

    def __init__(self, bot):
        self.bot = bot

    async def test(self):
        assert 1 + 1 == 2
        assert 1 + 1 == 2


def setup(bot):
    cog = Test(bot)
    bot.add_listener(cog.test, "on_ready")
    bot.add_cog(cog)