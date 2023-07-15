#!/bin/bash

# load environment variables from the config.env file
source config.env

# start the clojure program with environment variables
clojure -M -m stopwatch-bot.core --channel $CHANNEL --token "oauth:$TOKEN" --username $USERNAME --font $FONT --allowed $ALLOWED
