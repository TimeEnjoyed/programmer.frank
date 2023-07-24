FROM ubuntu:latest

WORKDIR /app
COPY . /app

RUN apt update && apt install openjdk-18-jdk curl libx11-dev libxtst-dev libxrender-dev libxext-dev -y
RUN curl -O https://download.clojure.org/install/linux-install-1.11.1.1347.sh
RUN chmod +x linux-install-1.11.1.1347.sh
RUN ./linux-install-1.11.1.1347.sh

RUN apt install rlwrap -y
RUN clj -P

CMD ["clojure", "-M", "-m", "stopwatch-bot.core"]