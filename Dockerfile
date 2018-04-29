FROM cookkkie/ffmpeg

RUN apt-get update && apt-get upgrade -y
RUN apt-get install libffi-dev -y
RUN apt-get install git -y
RUN apt-get install telnet -y

WORKDIR /
ADD requirements.txt /requirements.txt
RUN pip3 install --upgrade pip
RUN pip3 install --upgrade -r requirements.txt
RUN pip3 install --upgrade git+https://github.com/Rapptz/discord.py@rewrite#egg=discord.py[voice]

ADD . /

CMD ["python3", "oofster.py"]
