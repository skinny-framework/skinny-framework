@ECHO OFF

REM
REM skinny command for Windows
REM

SET command=%1
SET option=%2

IF NOT DEFINED command (
  GOTO show_help
)

IF EXIST "ivy2" (
  XCOPY ivy2\* %HOMEPATH%\.ivy2\. /E /D /q
  RMDIR ivy2 /s /q
)

IF %command%==run (
  GOTO run
)
IF %command%==server (
  GOTO run
)
IF %command%==s (
  GOTO run
)

IF %command%==debug (
  GOTO debug
)
IF %command%==d (
  GOTO debug
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
IF "%command%"=="~compile" (
  sbt "project dev" "~;compile"
  GOTO script_eof
)

IF %command%==test (
  SET SKINNY_ENV=test
  sbt "dev/test"
  GOTO script_eof
)
IF "%command%"=="~test" (
  SET SKINNY_ENV=test
  sbt "project dev" "~;test"
  GOTO script_eof
)
IF %command%==test-quick (
  SET SKINNY_ENV=test
  sbt "dev/testQuick"
  GOTO script_eof
)
IF %command%==testQuick (
  SET SKINNY_ENV=test
  sbt "dev/testQuick"
  GOTO script_eof
)
IF "%command%"=="~test-quick" (
  SET SKINNY_ENV=test
  sbt "project dev" "~;testQuick"
  GOTO script_eof
)
IF "%command%"=="~testQuick" (
  SET SKINNY_ENV=test
  sbt "project dev" "~;testQuick"
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
IF "%command%"=="~test-only" (
  SET SKINNY_ENV=test
  sbt "project dev" "~;testOnly %2"
  GOTO script_eof
)
IF "%command%"=="~testOnly" (
  SET SKINNY_ENV=test
  sbt "project dev" "~;testOnly %2"
  GOTO script_eof
)

IF "%command%"=="test:coverage" (
  SET SKINNY_ENV=test
  sbt "dev/scoverage:test"
  GOTO script_eof
)

IF "%command%"=="scalajs:watch" (
  SET SUB_COMMAND="~;packageJS"
  GOTO scalajs_task
)
IF "%command%"=="scalajs:package" (
  SET SUB_COMMAND="packageJS"
  GOTO scalajs_task
)
IF "%command%"=="scalajs:optimize" (
  SET SUB_COMMAND="optimizeJS"
  GOTO scalajs_task
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

    RMDIR task\src\main\resources /S /q
    MKDIR task\src\main\resources
    XCOPY src\main\resources task\src\main\resources /E /D /q
    sbt "task/run generate:%generator_params%"
  )
  GOTO script_eof
)

IF "%command%"=="db:migrate" (
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run db:migrate %2"
  GOTO script_eof
)

IF "%command%"=="db:repair" (
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run db:repair %2"
  GOTO script_eof
)

IF %command%==eclipse (
  IF NOT EXIST "project\_skinny_eclipse.sbt" (
    ECHO addSbtPlugin^(^"com.typesafe.sbteclipse^" %% ^"sbteclipse-plugin^" %% ^"2.4.0^"^) > "project\_skinny_eclipse.sbt"
  )
  sbt eclipse
  GOTO script_eof
)

SET is_gen_idea=false
IF "%command%"=="idea"     SET is_gen_idea=true
IF "%command%"=="gen-idea" SET is_gen_idea=true
IF "%is_gen_idea%"=="true" (
  IF NOT EXIST "project\_skinny_idea.sbt" (
    ECHO addSbtPlugin^(^"com.github.mpeltonen^" %% ^"sbt-idea^" %% ^"1.6.0^"^) > "project\_skinny_idea.sbt"
  )
  sbt gen-idea
  GOTO script_eof
)

IF %command%==package (
  RMDIR build /S /q
  MKDIR build
  XCOPY src\* build\src\* /E /D /q
  xcopy build.sbt build\ /S /q
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "build/package"
  GOTO script_eof
)

IF "%command%"=="package:standalone" (
  IF NOT EXIST "project\_skinny_assembly.sbt" (
    ECHO addSbtPlugin^(^"com.eed3si9n^" %% ^"sbt-assembly^" %% ^"0.11.2^"^) > "project\_skinny_assembly.sbt"

    SET SETTINGS_FILE="_skinny_assembly_settings.sbt"
    ECHO import AssemblyKeys._ > "_skinny_assembly_settings.sbt"
    ECHO. >> "_skinny_assembly_settings.sbt"
    ECHO assemblySettings >> "_skinny_assembly_settings.sbt"
    ECHO. >> "_skinny_assembly_settings.sbt"
    ECHO mainClass in assembly := Some^(^"skinny.standalone.JettyLauncher^"^) >> "_skinny_assembly_settings.sbt"
    ECHO. >> "_skinny_assembly_settings.sbt"
    ECHO _root_.sbt.Keys.test in assembly := {} >> "_skinny_assembly_settings.sbt"
  )
  RMDIR standalone-build /S /q
  MKDIR standalone-build
  XCOPY src\* standalone-build\src\* /E /D /q
  XCOPY build.sbt standalone-build\ /q
  xcopy _skinny_assembly_settings.sbt standalone-build\ /q
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "standalone-build/assembly"
  GOTO script_eof
)

IF %command%==publish (
  rmdir build /S /q
  mkdir build
  xcopy src\* build\src\* /E /D /q
  xcopy build.sbt build\ /q
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "build/publish"
  GOTO script_eof
)

REM ***************************************************************************
REM Didn't select command.
REM ***************************************************************************
:show_help
ECHO.
ECHO  Usage: skinny [COMMAND] [OPTIONS]...
ECHO.
ECHO   run/server/s       : will run application for local development
ECHO   debug/d            : will run application with JDWP. default port 5005
ECHO   clean              : will clear target directory
ECHO   update             : will run sbt update
ECHO   console            : will run sbt console
ECHO   compile            : will compile all the classes
ECHO   ~compile           : will compile all the classes when changes are detected
ECHO   db:migrate         : will execute database migration
ECHO   db:repair          : will recover when previous migration failed
ECHO   test               : will run all the tests
ECHO   ~test              : will run all the tests when changes are detected
ECHO   testQuick          : will run only failed tests
ECHO   ~testQuick         : will run only failed tests when changes are detected
ECHO   testOnly           : will run the specified test
ECHO   ~testOnly          : will run the specified test when changes are detected
ECHO   test:coverage      : will run all the tests and output coverage reports
ECHO   package            : will create *.war file to deploy
ECHO   package:standalone : will create *.jar file to run as stand alone app
ECHO   publish            : will publish *.war file to repository
ECHO.
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
GOTO script_eof

REM ***************************************************************************
REM run command
REM ***************************************************************************
:run
IF "%option%"=="-precompile" (
  sbt "project precompileDev" "~;container:restart"
) ELSE IF "%option%"=="--precompile" (
  sbt "project precompileDev" "~;container:restart"
) ELSE (
  sbt "~;container:restart"
)
GOTO script_eof

REM ***************************************************************************
REM debug command
REM ***************************************************************************
:debug
IF "%option%"=="-precompile" (
  IF "%3"=="" (
    sbt-debug 5005 "project precompileDev" "~;container:restart"
  ) ELSE (
    sbt-debug %3 "project precompileDev" "~;container:restart"
  )
) ELSE IF "%option%"=="--precompile" (
  IF "%3"=="" (
    sbt-debug 5005 "project precompileDev" "~;container:restart"
  ) ELSE (
    sbt-debug %3 "project precompileDev" "~;container:restart"
  )
) ELSE (
  IF "%2"=="" (
    sbt-debug 5005 "~;container:restart"
  ) ELSE (
    sbt-debug %2 "~;container:restart"
  )
)
GOTO script_eof

:scalajs_task
IF NOT EXIST "project\_skinny_scalajs.sbt" (
  ECHO addSbtPlugin^("org.scala-lang.modules.scalajs" %% "scalajs-sbt-plugin" %% "0.4.3"^) > "project\_skinny_scalajs.sbt"

  ECHO lazy val scalaJS = Project^(id = "scalajs", base = file^("src/main/webapp/WEB-INF/assets"^),  > "_skinny_scalajs_settings.sbt"
  ECHO   settings = Defaults.defaultSettings ++ Seq^(      >> "_skinny_scalajs_settings.sbt"
  ECHO     name := "application", // JavaScript file name  >> "_skinny_scalajs_settings.sbt"
  ECHO     unmanagedSourceDirectories in Compile ^<+= baseDirectory^(_ / "scala"^), >> "_skinny_scalajs_settings.sbt"
  ECHO     libraryDependencies ++= Seq^(                   >> "_skinny_scalajs_settings.sbt"
  ECHO       "org.scala-lang.modules.scalajs" %%%% "scalajs-dom"                    %% "0.3", >> "_skinny_scalajs_settings.sbt"
  ECHO       "org.scala-lang.modules.scalajs" %%%% "scalajs-jquery"                 %% "0.3", >> "_skinny_scalajs_settings.sbt"
  ECHO       "org.scala-lang.modules.scalajs" %%%% "scalajs-jasmine-test-framework" %% "0.4.3" %% "test" >> "_skinny_scalajs_settings.sbt"
  ECHO     ^), >> "_skinny_scalajs_settings.sbt"
  ECHO     crossTarget in Compile ^<^<= baseDirectory^(_ / ".." / ".." / "assets" / "js"^) >> "_skinny_scalajs_settings.sbt"
  ECHO   ^) >> "_skinny_scalajs_settings.sbt"
  ECHO ^) >> "_skinny_scalajs_settings.sbt"
)
sbt "project scalajs" %SUB_COMMAND%
GOTO script_eof


:script_eof


