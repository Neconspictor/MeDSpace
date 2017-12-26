@java -cp "h2-1.4.196.jar;%H2DRIVERS%;%CLASSPATH%" org.h2.tools.Console %* -tcp -tcpAllowOthers -tcpPort 5000
@if errorlevel 1 pause