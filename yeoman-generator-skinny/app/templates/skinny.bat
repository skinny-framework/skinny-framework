@ECHO OFF

REM
REM skinny command for Windows
REM

SET command=%1

IF NOT DEFINED command (
  GOTO :message
)

IF %command%==run (
  sbt "project dev" "~;container:stop;container:start"
  GOTO :end
)

IF %command%==clean (
  sbt clean
  GOTO :end
)

IF %command%==update (
  sbt update
  GOTO :end
)

IF %command%==console (
  sbt "dev/console"
  GOTO :end
)

IF %command%==compile (
  sbt "dev/compile"
  GOTO :end
)

IF %command%==test (
  sbt "dev/test"
  GOTO :end
)

IF %command%==test-only (
  sbt "dev/test-only %2"
  GOTO :end
)

IF "%command%"=="g"        SET is_generator=true
IF "%command%"=="generate" SET is_generator=true
IF "%is_generator"=="true" (
  SET generator_type=%2
  IF "%generator_type%"=="scaffold" (
    sbt "task/run generate-scaffold %3 %4 %5 %6 %7 %8 %9"
    GOTO :end
  )
  IF "%generator_type%"=="controller" (
    sbt "task/run generate-controller %3 %4 %5 %6 %7 %8 %9"
    GOTO :end
  )
  IF "%generator_type%"=="model" (
    sbt "task/run generate-model %3 %4 %5 %6 %7 %8 %9"
    GOTO :end
  )
  ECHO Usage: ./skinny g/generate [type] [options...]
  GOTO :end
)

IF "%command%"=="db:migrate" (
  rmdir task\src\main\resources /s /q
  mkdir task\src\main\resources
  xcopy src\main\resources task\src\main\resources /E /D
  sbt "task/run db:migrate %2"
  GOTO :end
)

IF %command%==package (
  rmdir build /s /q
  mkdir build
  xcopy src\* build\src\* /E /D
  xcopy build.sbt build\build.sbt /E /D
  sbt "task/run assets:precompile" "build/package"
  GOTO :end
)

IF %command%==publish (
  rmdir build /s /q
  mkdir build
  xcopy src\* build\src\* /E /D
  xcopy build.sbt build\build.sbt /E /D
  sbt "task/run assets:precompile" "build/publish"
  GOTO :end
)

REM Didn't select command.
:message
ECHO.
ECHO Usage: skinny [COMMAND] [OPTIONS]...
ECHO.
ECHO   run        : will run Skinny app for local development
ECHO   clean      : will clear target directory
ECHO   update     : will update dependencies
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
ECHO   g/generate scaffold   : will generate scaffold files

:end


