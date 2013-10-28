@echo off

REM
REM skinny command for Windows
REM

set COMMAND=%1

IF NOT DEFINED COMMAND (
GOTO :message
)

IF %COMMAND%==run (
sbt "~;container:stop;container:start"
GOTO :end
)

IF %COMMAND%==clean (
  sbt clean
  GOTO :end
)

IF %COMMAND%==update (
  sbt update
  GOTO :end
)

IF %COMMAND%==console (
  sbt console
  GOTO :end
)

IF %COMMAND%==compile (
  sbt compile
  GOTO :end
)

IF %COMMAND%==test (
  sbt test
  GOTO :end
)

IF %COMMAND%==test-only (
  sbt test-only %2
  GOTO :end
)

IF %COMMAND%==package (
  sbt package
  GOTO :end
)

REM Didn't select command.
:message
echo.
echo Usage: skinny [COMMAND] [OPTIONS]...
echo.
echo   clean     : will clear target directory
echo   run       : will run Skinny app for local development
echo   compile   : will compile all the classes
echo   update    : will update dependencies
echo   test      : will run all the tests
echo   test-only : will run the specified test
echo   package   : will create *.war file to deploy
echo.


:end
