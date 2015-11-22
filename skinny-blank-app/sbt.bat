set SCRIPT_DIR=%~dp0
java -Xmx1024M -Xss1M -Dinput.encoding=Cp1252 -Dfile.encoding=SJIS -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -jar "%SCRIPT_DIR%bin\sbt-launch.jar" %*

