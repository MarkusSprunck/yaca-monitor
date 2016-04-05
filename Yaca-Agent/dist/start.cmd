@echo off 
 
set JAVA_HOME=C:/Program Files/Java/jdk1.8.0_77
set PATH="%JAVA_HOME%\bin";%PATH%
 
cmd /c "start /min  java -Xint  -jar YacaAgentTestClient.jar"
cmd /c "start /min  chrome http://localhost:33333/monitor"
java -jar YacaAgent.jar"
 