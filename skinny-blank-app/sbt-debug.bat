set SCRIPT_DIR=%~dp0
set DEBUG_PORT=%1
shift
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=127.0.0.1:%DEBUG_PORT% -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -jar "%SCRIPT_DIR%bin\sbt-launch.jar" %1 %2 %3 %4 %5 %6 %7 %8 %9
