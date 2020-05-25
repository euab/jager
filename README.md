<p align="center">
    <img src="https://i.imgur.com/8DZxj6X.png" height="300" width="500">
    <h1 align="center">Jäger V2</h1>
    <p align="center"><b>The development branch for the next major version of the Jäger Discord bot.</b></p>
</p>

<p align="center">
    <a href="https://travis-ci.org/github/Euab/jager">
        <img alt="Travis (.org) branch" src="https://img.shields.io/travis/euab/jager/master?style=for-the-badge">
    </a>
    <a href="https://www.codacy.com/app/Euab/jager">
        <img alt="Codacy branch grade" src="https://img.shields.io/codacy/grade/fa73126c883d4b769c68b403aa0acb7d/mastger?style=for-the-badge">
    </a>
    <a href="https://discord.com/oauth2/authorize?client_id=495551162737754122&scope=bot&permissions=8">
        <img alt="Discord invite link" src="https://img.shields.io/badge/Discord-Invite-blueviolet?logo=discord&style=for-the-badge">
    </a>
    <a href="https://www.gnu.org/licenses/agpl-3.0.en.html">
        <img alt="GitHub" src="https://img.shields.io/github/license/euab/jager?style=for-the-badge">
    </a>
</p>

> It is not recommended that you try and host your own instance of this bot. Not only is this branch very unstable and
> subject to constant updates, but the bot is also built for the environment that it runs in. It will likely fail if
> you try to run it yourself without experience of JDA and Java.

## Task list
This is the list of all of the tasks that I intend to implement. Each task will be checked off as they are completed and
some tasks may be added or removed during the development process.

- [x] Basic functionality
- [x] Ping command
- [x] General utility and helper commands
- [x] Search commands
- [x] Music player
- [ ] Database implementation
- [ ] System commands
- [ ] Levelling system
- [ ] General commands
- [ ] Moderation
- [ ] AI implementation
- [ ] Jager REST API
- [ ] Online server dashboard

## Building your very own Jager

### ⚠ Read before attempting
We don't recommend that you build your own instance of Jager to run. It is not a documented project and most builds
here will be bleeding edge and will not be stable. Likely including error-filled code and unfinished features.

<sub>
I am literally the master of bodging code so the bit above will be the case 80% of the time
</sub>

### Build-a-bot
#### Prerequisites
- JDK
- Gradle
- An IDE such as IntelliJ (if you are editing code)

#### Steps for building
<sub>
There may be extra steps in building the bot if any errors occur.
</sub>

1. Make sure that all prerequisites are installed and running. You can check Java by opening a terminal and typing
```bash
$ java -version
```

2. Clone this repository using the command below (you can also fork this repo and clone your own fork).
```bash
$ git clone https://github.com/euab/jager.git
```

3. Change your working directory to the root folder
```bash
$ cd jager
```

4. Checkout to the `v2` branch.
```bash
$ git checkout v2
```

5. Open a terminal in the root folder of the project and run:
```bash
$ gradlew shadowJar
```

6. Run the bot using the command below and prepare for the configuration errors and to start having to fill in missing
configs.
```bash
$ java -jar Jager.jar
```
The bot will likely crash with a `NullPointerException` as it cannot load the configuration file. So you'll have to make
it yourself.

7. Change your working directory to the resources directory.
```bash
cd src/main/resources
```
Then make a new file called `configuration.json`.
```bash
$ echo {} >> configuration.json
```

You will need to format the JSON file in this manner, otherwise the bot will not work. As you are likely on a command
line it would likely be easiest to just Vim into the file but you can use any text editor you like as long as you indent
the JSON correctly.

```bash
$ vi configuration.json
```

```json
{
  "environment": "production",
  "botAuth": {
    "token": "YOUR DISCORD TOKEN HERE",
    "oauth": "YOUR OAUTH URL HERE",
    "activationDelay": "0"
  }
}
```

8. Then back out back to the root directory.
```bash
$ cd ../../..
```

Finally run the bot

```bash
$ java -jar Jager.jar
```

**You now have a running instance of Jager.**

