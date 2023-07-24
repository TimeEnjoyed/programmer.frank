# Stopwatch Bot
This is a Clojure application that provides a stopwatch bot.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites
You need to have [Clojure](https://clojure.org/guides/install_clojure) installed on your machine. 

### Installation
Clone the repository:
```bash
git clone https://github.com/frankbuss/stopwatchbot.git
```
Navigate into the project directory:
```bash
cd stopwatchbot
```
Copy the template environment file to create your own config.env:
```bash
cp config.edn.template config.edn
```
Open the config.edn file and change the login credentials, and allowed users who can use the `!sw` chat bot command. [Here](https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/) is explained how to get the OAUTH token for Twitch. Optionally you can adjust the other settings as well.

## Usage
Start the bot by running the run.sh script:
```bash
./run.sh
```
You will see output like this:
```
Starting bot...
Connection established...
Sending token
Sending: NICK 10x_programmer
Sending: JOIN #10x_programmer
Sending: PRIVMSG #10x_programmer :stopwatch-bot ready

Received:  :tmi.twitch.tv 001 10x_programmer :Welcome, GLHF!
:tmi.twitch.tv 002 10x_programmer :Your host is tmi.twitch.tv
:tmi.twitch.tv 003 10x_programmer :This server is rather new
:tmi.twitch.tv 004 10x_programmer :-
:tmi.twitch.tv 375 10x_programmer :-
:tmi.twitch.tv 372 10x_programmer :You are in a maze of twisty passages, all alike.
:tmi.twitch.tv 376 10x_programmer :>
Received:  :10x_programmer!10x_programmer@10x_programmer.tmi.twitch.tv JOIN #10x_programmer
:10x_programmer.tmi.twitch.tv 353 10x_programmer = #10x_programmer :10x_programmer
:10x_programmer.tmi.twitch.tv 366 10x_programmer #10x_programmer :End of /NAMES list
```
and a window like this will open:

![Window Screenshot](window.png)

The magenta background (color `#ff00ff`) makes it easier to integrate it in OBS and to overlay it with transparent background, if desired. See [here](https://obsproject.com/kb/color-key-filter) how to do it.

From the chat where the bot has joined, you can now start the countdown. There are three ways to specify the time:

1. Seconds:
   - Example: `!sw 35` represents 35 seconds.

1. Minutes:Seconds:
   - Example: `!sw 2:45` represents 2 minutes and 45 seconds.

1. Hours:Minutes:Seconds:
   - Example: `!sw 1:15:30` represents 1 hour, 15 minutes, and 30 seconds.

On the terminal, you can also enter chat messages, which will be sent to the IRC server. You can stop the bot with ctrl-c.


# Docker Instructions (Windows)
*Follow the above README instructions, including creating a config file**


**Download and Install:**
[VcXsrv](https://sourceforge.net/projects/vcxsrv/)


- Launch `XLaunch`
  - Multiple Windows
  - Display number: -1
  - `Next >`
  - Start no client
  - `Next >`
  - `Keep all deafults`
  - `Next >`
  - `Finish`


*You won't notice anything popup just yet*


**Build and Run Docker**
```shell
docker-compose build
docker-compose up
```

**After Demo:**
- Close Docker
- Close XLaunch by Right Clicking Tray Icon and Exit.