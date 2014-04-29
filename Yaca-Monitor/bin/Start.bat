echo off
echo.
echo YACA-AGENT - STATIC AND DYNAMIC ANALYSIS
echo.
IF exist PID.TXT del PID.TXT

set JAVA_HOME=C:\Tools\java\jdk1.6.0_43
set path=%JAVA_HOME%\bin;%path%

REM Start a test client. This client creates a file with the process id of the test client.
echo Start YacaAgentTestClient...
start javaw -Xint -jar YacaAgentTestClient.jar
echo.

REM Start the YacaAgent
echo Start YacaAgent...
echo.

java  -cp "%JAVA_HOME%\lib\tools.jar;YacaAgent.jar" com.sw_engineering_candies.yaca.Agent

pause