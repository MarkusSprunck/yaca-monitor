#!/bin/sh

echo "YACA-AGENT - STATIC AND DYNAMIC ANALYSIS"

JAVA_RUN_TIME=/Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home/bin/java
JAR1=/Users/markus/git/yaca-monitor/Yaca-Agent/dist/YacaAgentTestClient.jar
JAR2=/Users/markus/git/yaca-monitor/Yaca-Agent/dist/YacaAgent.jar

if [ -f "$JAR1" ]; then
  echo "Start yaca agent test client."
  $JAVA_RUN_TIME -Xint -jar "$JAR1" &
else
  echo "File YacaAgentTestClient.jar doesn't exist."
  exit 1
fi

/usr/bin/open -a "/Applications/Google Chrome.app" 'http://localhost:33333/monitor' 

echo "Start Yaca Agent."
$JAVA_RUN_TIME  -jar "$JAR2"

