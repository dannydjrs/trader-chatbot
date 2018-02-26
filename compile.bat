@echo off

REM dir "$(dirname "$0")"

set classpath=.;jsoup-1.11.2.jar;JSON-parser.jar 
set folders=.\virtualassistant\ai\*.java .\virtualassistant\chatbot\*.java .\virtualassistant\data\news\*.java .\virtualassistant\data\system\*.java .\virtualassistant\data\stocks\*.java .\virtualassistant\data\datastore\*.java .\virtualassistant\gui\*.java .\virtualassistant\misc\*.java 

REM Compile all java files

javac -cp %classpath% %folders% virtualassistant\IVirtualAssistant.java virtualassistant\VirtualAssistant.java 

pause