#!/bin/sh

JAVA_RUN_TIME=/Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home/bin/java
JAR1=/Users/markus/git/yaca-monitor/Yaca-Agent/dist/YacaAgentTestClient.jar
JAR2=/Users/markus/git/yaca-monitor/Yaca-Agent/dist/YacaAgent.jar

$JAVA_RUN_TIME -Xint -jar "$JAR1" &
$JAVA_RUN_TIME  -jar "$JAR2" & 

sleep 3
/usr/bin/open -a "/Applications/Google Chrome.app" 'http://localhost:33333/monitor' 

