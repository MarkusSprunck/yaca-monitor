#!/bin/sh

java  -Xint -jar YacaAgentTestClient-jar-with-dependencies.jar  &

java  -classpath "../lib/tools.jar" -jar YacaAgent-jar-with-dependencies.jar  &

sleep 3

/usr/bin/open -a "/Applications/Google Chrome.app" 'http://localhost:8082/monitor'

