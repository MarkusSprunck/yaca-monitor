#!/bin/sh
JAR1=/Users/markus/git/yaca-monitor/Yaca-Monitor/bin/YacaAgentTestClient.jar
if [ -f "$JAR1" ]; then
  echo "File YacaAgentTestClient.jar exists."
  /Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home/bin/java -Xint -jar "$JAR1" &
else
  echo "File YacaAgentTestClient.jar doesn't exist."
  exit 1
fi

JAR2=/Users/markus/git/yaca-monitor/Yaca-Monitor/bin/YacaAgent.jar
/Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home/bin/java  -jar "$JAR2"
