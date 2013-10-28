@echo off

REM
REM skinny command for Windows
REM

set COMMAND=%1

IF NOT DEFINED COMMAND (
GOTO :message
)

IF %COMMAND%==run (
sbt "project dev" "~;container:stop;container:start"
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
  sbt "dev/console"
  GOTO :end
)

IF %COMMAND%==compile (
  sbt "dev/compile"
  GOTO :end
)

IF %COMMAND%==test (
  sbt "dev/test"
  GOTO :end
)

IF %COMMAND%==test-only (
  sbt "dev/test-only %2"
  GOTO :end
)

IF %COMMAND%==package (
  rmdir build /s /q
  mkdir build
  xcopy src build\src /E /D
  xcopy build.sbt build\build.sbt /E /D
  sbt "build/run" "build/package"
  GOTO :end
)

REM Didn't select command.
:message
echo.
echo Usage: skinny [COMMAND] [OPTIONS]...
echo.
echo   run       : will run Skinny app for local development
echo   clean     : will clear target directory
echo   update    : will update dependencies
echo   console   : will run sbt console
echo   compile   : will compile all the classes
echo   test      : will run all the tests
echo   test-only : will run the specified test
echo   package   : will create *.war file to deploy
echo.


:end
