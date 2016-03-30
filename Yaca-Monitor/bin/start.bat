
echo YACA-AGENT - STATIC AND DYNAMIC ANALYSIS

set JAVA_HOME=C:\Tools\java\jdk1.6.0_43
set path=%JAVA_HOME%\bin;%path%

echo Start yaca agent test client.
start javaw -Xint -jar YacaAgentTestClient.jar
echo.

echo Start yaca agent.
java -cp "%JAVA_HOME%\lib\tools.jar;YacaAgent.jar" com.sw_engineering_candies.yaca.Agent

pause