echo off
echo.
echo YACA-AGENT - STATIC AND DYNAMIC ANALYSIS
echo.
IF exist PID.TXT del PID.TXT

set JAVA_HOME=D:\Tools\java\jdk1.7.0
set path=%JAVA_HOME%\bin;%path%

REM Version of java is just printed for information
java -version
echo.

REM Start a test client. This client creates a file with the process id of the test client.
echo Start YacaAgentTestClient...
start javaw -Xint -jar YacaAgentTestClient.jar
echo.

REM Wait for the file with process id, store in variable and delete file. 
:NEXT_1
IF not exist PID.TXT  goto NEXT_1
set /p var_pid= < PID.TXT
del PID.TXT
echo.

REM Start the YacaAgent
echo Start YacaAgent...
echo.

java  -cp "%JAVA_HOME%\lib\tools.jar;YacaAgent.jar" com.sw_engineering_candies.yaca.Agent -port 33333  -id %var_pid%

pause