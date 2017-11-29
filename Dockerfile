FROM python:3
RUN apt-get upgrade -y && apt-get update
RUN apt-get install -y libffi-dev
RUN apt-get install -y git
RUN apt-get install telnet -y
WORKDIR /
ADD requirements.txt /requirements.txt
RUN pip3 install --upgrade pip
RUN pip3 install --upgrade -r requirements.txt
RUN python3 -m pip install git+https://github.com/Rapptz/discord.py@rewrite#egg=discord.py[voice]
ADD . /
CMD ["python", "rickbot.py"]
