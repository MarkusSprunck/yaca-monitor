echo off
echo.
echo YACA-AGENT - STATIC AND DYNAMIC ANALYSIS
echo.

REM Start a test client. 
echo Start YacaAgentTestClient...
start javaw -Xint -jar YacaAgentTestClient.jar
echo.

REM Start the YacaAgent
echo Start YacaAgent...
echo.
java -cp "%JAVA_HOME%\lib\tools.jar;YacaAgent.jar" com.sw_engineering_candies.yaca.Agent

pause