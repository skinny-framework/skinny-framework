@ECHO OFF

REM
REM skinny command for Windows
REM

SET command=%1
SET option=%2

IF NOT DEFINED command (
  GOTO show_help
)

IF %command%==run (
  IF "%option%"=="-precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE IF "%option%"=="--precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE (
      sbt "~;container:stop;container:start"
  )
  GOTO script_eof
)
IF %command%==server (
  IF "%option%"=="-precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE IF "%option%"=="--precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE (
      sbt "~;container:stop;container:start"
  )
  GOTO script_eof
)
IF %command%==s (
  IF "%option%"=="-precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE IF "%option%"=="--precompile" (
    sbt "project precompileDev" "~;container:stop;container:start"
  ) ELSE (
      sbt "~;container:stop;container:start"
  )
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
  SET SKINNY_ENV=test
  sbt "dev/test"
  GOTO script_eof
)
IF %command%==test-quick (
  SET SKINNY_ENV=test
  sbt "dev/test-quick"
  GOTO script_eof
)
IF %command%==testQuick (
  SET SKINNY_ENV=test
  sbt "dev/test-quick"
  GOTO script_eof
)
IF %command%==test-only (
  SET SKINNY_ENV=test
  sbt "dev/test-only %2"
  GOTO script_eof
)
IF %command%==testOnly (
  SET SKINNY_ENV=test
  sbt "dev/test-only %2"
  GOTO script_eof
)
IF %command%=="test:coverage" (
  SET SKINNY_ENV=test
  sbt "dev/scoverage:test"
  GOTO script_eof
)

IF %command%=="scalajs:watch" (
  sbt "project scalajs" "~;packageJS"
  GOTO script_eof
)
IF %command%=="scalajs:package" (
  sbt "project scalajs" packageJS
  GOTO script_eof
)
IF %command%=="scalajs:optimize" (
  sbt "project scalajs" optimizeJS
  GOTO script_eof
)

SET is_generator=false
SET generator_params=
IF "%command%"=="g"        SET is_generator=true
IF "%command%"=="generate" SET is_generator=true
IF "%is_generator%"=="true" (
  IF "%2"=="" (
    ECHO Usage: skinny g/generate [type] [options...]
  ) ELSE (
    :generator_loop_begin
      IF "%2"=="" GOTO generator_loop_end
        SET generator_params=%generator_params% %2
      SHIFT
      GOTO generator_loop_begin
    :generator_loop_end
    REM Delete the head whitespace character
    SET generator_params=%generator_params:~1%

    RMDIR task\src\main\resources /s /q
    MKDIR task\src\main\resources
    XCOPY src\main\resources task\src\main\resources /E /D /q
    sbt "task/run generate:%generator_params%"
  )
  GOTO script_eof
)

IF "%command%"=="db:migrate" (
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run db:migrate %2"
  GOTO script_eof
)

IF "%command%"=="db:repair" (
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run db:repair %2"
  GOTO script_eof
)

IF %command%==eclipse (
  sbt eclipse
  GOTO script_eof
)

SET is_gen_idea=false
IF "%command%"=="idea"     SET is_gen_idea=true
IF "%command%"=="gen-idea" SET is_gen_idea=true
IF "%is_gen_idea%"=="true" (
  sbt gen-idea
  GOTO script_eof
)

IF %command%==package (
  RMDIR build /s /q
  MKDIR build
  XCOPY src\* build\src\* /E /D /q
  XCOPY build.sbt build\build.sbt /E /D /q
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "build/package"
  GOTO script_eof
)

IF "%command%"=="package:standalone" (
  RMDIR standalone-build /s /q
  MKDIR standalone-build
  XCOPY src\* standalone-build\src\* /E /D /q
  XCOPY build.sbt standalone-build\build.sbt /E /D /q
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "standalone-build/assembly"
  GOTO script_eof
)

IF %command%==publish (
  rmdir build /s /q
  mkdir build
  xcopy src\* build\src\* /E /D /q
  xcopy build.sbt build\build.sbt /E /D /q
  RMDIR task\src\main\resources /s /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "build/publish"
  GOTO script_eof
)

REM Didn't select command.
:show_help
ECHO.
ECHO  Usage: skinny [COMMAND] [OPTIONS]...
ECHO.
ECHO   run/server/s   : will run application for local development
ECHO   clean          : will clear target directory
ECHO   update         : will run sbt update
ECHO   console        : will run sbt console
ECHO   compile        : will compile all the classes
ECHO   db:migrate     : will execute database migration
ECHO   db:repair      : will recover when previous migration failed
ECHO   test           : will run all the tests
ECHO   test-quick     : will run only failed tests
ECHO   test-only      : will run the specified test
ECHO   test:coverage  : will run all the tests and output coverage reports
ECHO   package            : will create *.war file to deploy
ECHO   package:standalone : will create *.jar file to run as stand alone app
ECHO   publish            : will publish *.war file to repository
ECHO.
ECHO   # Scala.js trial is disabled by default
ECHO   scalajs:watch    : will watch Scala.js Scala code change and convert to JS
ECHO   scalajs:package  : will convert Scala.js Scala code to JS file
ECHO   scalajs:optimize : will optimize the huge JS file to optimized small JS
ECHO.
ECHO   eclipse       : will setup Scala IDE settings
ECHO   idea/gen-idea : will setup IntelliJ IDEA settings
ECHO.
ECHO   g/generate controller : will generate controller
ECHO   g/generate model      : will generate model
ECHO   g/generate migration  : will generate db migration file
ECHO.
ECHO   g/generate scaffold       : will generate scaffold files with ssp templates
ECHO   g/generate scaffold:scaml : will generate scaffold files with scaml templates
ECHO   g/generate scaffold:jade  : will generate scaffold files with jade templates
ECHO.
ECHO   g/generate reverse-scaffold       : will generate from existing database
ECHO   g/generate reverse-scaffold:scaml : will generate from existing database
ECHO   g/generate reverse-scaffold:jade  : will generate from existing database

:script_eof

