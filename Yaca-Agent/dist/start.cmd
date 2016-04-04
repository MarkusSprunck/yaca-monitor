echo YACA-AGENT - STATIC AND DYNAMIC ANALYSIS
 
SET JDK_BIN_PATH="C:/Program Files/Java/jdk1.8.0_77/bin/java.exe"
 
echo Start yaca agent test client.
start %JDK_BIN_PATH% -Xint  -jar ".\YacaAgentTestClient.jar"
echo.
 
echo Start yaca agent.
%JDK_BIN_PATH% -jar ".\YacaAgent.jar"
 