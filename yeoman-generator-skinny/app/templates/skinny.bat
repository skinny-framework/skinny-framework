@ECHO OFF

REM
REM skinny command for Windows
REM

SET command=%1

IF NOT DEFINED command (
  GOTO show_help
)

IF %command%==run (
  sbt "project dev" "~;container:stop;container:start"
  GOTO script_eof
)

IF %command%==clean (
  sbt clean
  GOTO script_eof
)

IF %command%==update (
  sbt update
  GOTO script_eof
)

IF %command%==console (
  sbt "dev/console"
  GOTO script_eof
)

IF %command%==compile (
  sbt "dev/compile"
  GOTO script_eof
)

IF %command%==test (
  sbt "dev/test"
  GOTO script_eof
)

IF %command%==test-only (
  sbt "dev/test-only %2"
  GOTO script_eof
)

SET is_generator=false
IF "%command%"=="g"        SET is_generator=true
IF "%command%"=="generate" SET is_generator=true
IF "%is_generator%"=="true" (
  IF "%2"=="" (
    ECHO Usage: skinny g/generate [type] [options...]
  ) ELSE (
    sbt "task/run generate:%2 %3 %4 %5 %6 %7 %8 %9"
  )
  GOTO script_eof
)

IF "%command%"=="db:migrate" (
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  ECHO ^<configuration^>^<appender name="console" class="ch.qos.logback.core.ConsoleAppender"^>^<encoder^>^<pattern^>%date %level [%thread] %logger{10} [%file:%line] %msg%n^</pattern^>^</encoder^>^</appender^>^<root level="warn"^>^<appender-ref ref="console"/^>^</root^>^</configuration^> > task/src/main/resources/logback.xml
  sbt "task/run db:migrate %2"
  GOTO script_eof
)

IF %command%==package (
  RMDIR build /s /q
  MKDIR build
  XCOPY src\* build\src\* /E /D /q
  XCOPY build.sbt build\build.sbt /E /D /q
  sbt "task/run assets:precompile" "build/package"
  GOTO script_eof
)

IF %command%==publish (
  rmdir build /s /q
  mkdir build
  xcopy src\* build\src\* /E /D /q
  xcopy build.sbt build\build.sbt /E /D /q
  sbt "task/run assets:precompile" "build/publish"
  GOTO script_eof
)

REM Didn't select command.
:show_help
ECHO.
ECHO Usage: skinny [COMMAND] [OPTIONS]...
ECHO.
ECHO   run        : will run Skinny app for local development
ECHO   clean      : will clear target directory
ECHO   update     : will update depscript_exitencies
ECHO   console    : will run sbt console
ECHO   compile    : will compile all the classes
ECHO   db:migrate : will run all the tests
ECHO   test       : will run all the tests
ECHO   test-only  : will run the specified test
ECHO   package    : will create *.war file to deploy
ECHO   publish    : will publish *.war file to repository
ECHO.
ECHO   g/generate controller : will generate controller
ECHO   g/generate model      : will generate model
ECHO   g/generate migration  : will generate db migration file
ECHO.
ECHO   g/generate scaffold       : will generate scaffold files with ssp templates
ECHO   g/generate scaffold:scaml : will generate scaffold files with scaml templates
ECHO   g/generate scaffold:jade  : will generate scaffold files with jade templates

:script_eof

