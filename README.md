# RickBot

[![Build Status](https://travis-ci.org/rickbotdiscord/rickbot.svg?branch=master)](https://travis-ci.org/rickbotdiscord/rickbot) [![PyPI](https://img.shields.io/badge/Python-3.6.3-green.svg)](https://www.python.org/downloads/)  [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a0d19fe4283b4288a5146caa5c0891c5)](https://www.codacy.com/app/Euab/rickbot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rickbotdiscord/rickbot&amp;utm_campaign=Badge_Grade)

A cool bot for Discord.

Please do not try to host your own instance of this bot. Just invite it by calling the `invite` command on the
[official Discord server](https://discord.gg/TzDbESZ).

## Getting started
By default. RickBot's prefix is `!`. This can be changed at anytime by writing `!prefix <new prefix>`.
After that you can invoke commands with the new prefix.
The prefix can consist of any alphanumeric character.

Throughout this guide I'm going to use `!` as the prefix. Replace this with your server's prefix.

To get help with a command type `!help <command name>` and the bot will show you the arguments to pass along with the command.

## Creating a dev environment

For people looking do work on the bot. Other people just invite it to your server using `!invite`

### 1. Install Python 3.6
Python 3.6 or later will be needed in order to run this bot. Older versions will not work as this bot uses 3.6 features.
Remember to install Pip as well.

### 2. Install deps
You can install these all in one go by opening a command line in the bot's root directory and typing:

```bash
$ pip3 install -r requirements.txt
```

The list of dependencies are as follows:

- discord.py (rewrite branch, Pip Git URL in requirements.txt)
- psutil
- youtube_dl
- colorthief
- aiomeasures

In order for voice to work you will need FFmpeg.
For Windows users visit [this link](https://www.ffmpeg.org)
and download the zip archive. Then extract ffmpeg.exe
into the bot's root directory.

For Linux users you can just use the command line.

```bash
$ sudo apt-get install ffmpeg
```

### 3. Set up config
When the bot starts. It'll look for either environment variables or a secrets.py file.
Either way is good enough but here are the variables needed:

    - TOKEN = bot token
    - GOOGLE_API_KEY = your Google API key
    - TWITCH_CLIENT_ID = your Twitch client ID
   
It's up to you how this is done. I still use secrets in production because I am a disappointment.

### 4. Set up the database
I'm using JSON right now but I am soon going to be moving over to SQL (SQLite or Postgresql).

As the Database is ignored (see .gitignore) you will need to make a database directory yourself.
To do this make a directory in the bot's root directory called `data`.

```bash
$ mkdir data
```

Now move into that directory

```bash
$ cd data
```

Then we will need the JSON file.

```bash
$ echo "{}" >> guild.json
```

### 5. Start the bot
This is as easy as typing
```bash
$ python3 rickbot.py
```

When the bot starts leave the command line open and the bot will do it's thing.
You do not need to worry about a cache as this is done for you and is deleted
each time the bot is restarted anyway.
