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

IF %command%==new (
  ECHO Sorry to say, this operation is not supported yet on Windows...
  GOTO script_eof
)
IF %command%==upgrade (
  ECHO Sorry to say, this operation is not supported yet on Windows...
  GOTO script_eof
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
  sbt "coverage" "dev/test"
  GOTO script_eof
)

IF "%command%"=="scalajs:watch" (
  SET SUB_COMMAND="~;fastOptJS"
  GOTO scalajs_task
)
IF "%command%"=="scalajs:package" (
  SET SUB_COMMAND="fullOptJS"
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
    RMDIR task\target /S /q
    MKDIR task\src\main\resources
    XCOPY src\main\resources task\src\main\resources /E /D /q
    sbt "task/run generate:%generator_params%"
  )
  GOTO script_eof
)

IF "%command%"=="task:clean" (
  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/clean"
  GOTO script_eof
)

SET is_task_run=false
SET task_run_params=
IF "%command%"=="task:run" SET is_task_run=true
IF "%is_task_run%"=="true" (
  :task_run_loop_begin
    IF "%2"=="" GOTO task_run_loop_end
      SET task_run_params=%task_run_params% %2
    SHIFT
    GOTO task_run_loop_begin
  :task_run_loop_end
  REM Delete the head whitespace character
  SET task_run_params=%task_run_params:~1%

  RMDIR task\src\main\resources /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run %task_run_params%"
  GOTO script_eof
)

IF "%command%"=="db:migrate" (
  RMDIR task\src\main\resources /S /q
  RMDIR task\target /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run db:migrate %2"
  GOTO script_eof
)

IF "%command%"=="db:repair" (
  RMDIR task\src\main\resources /S /q
  RMDIR task\target /S /q
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
  sbt gen-idea
  GOTO script_eof
)

IF %command%==package (
  RMDIR build /S /q
  MKDIR build
  XCOPY src\* build\src\* /E /D /q
  xcopy build.sbt build\ /S /q
  RMDIR task\src\main\resources /S /q
  RMDIR task\target /S /q
  MKDIR task\src\main\resources
  XCOPY src\main\resources task\src\main\resources /E /D /q
  sbt "task/run assets:precompile" "build/package"
  GOTO script_eof
)

IF "%command%"=="package:standalone" (
  IF NOT EXIST "project\_skinny_assembly.sbt" (
    ECHO addSbtPlugin^(^"com.eed3si9n^" %% ^"sbt-assembly^" %% ^"0.11.2^"^) > "project\_skinny_assembly.sbt"
    (
      ECHO import AssemblyKeys._
      ECHO.
      ECHO assemblySettings
      ECHO.
      ECHO mainClass in assembly := Some^(^"skinny.standalone.JettyLauncher^"^)
      ECHO.
      ECHO _root_.sbt.Keys.test in assembly := {}
      ECHO.
      ECHO.resourceGenerators in Compile ^<+= ^(resourceManaged, baseDirectory^) ^map { ^(managedBase, base^) =^>
      ECHO.  val webappBase = base / "src" / "main" / "webapp"
      ECHO.  for ^( ^(from, to^) ^<- ^webappBase ** "*" `pair` rebase(webappBase, managedBase / "main/"^) ^)
      ECHO.  yield {
      ECHO.    Sync.copy^(from, to^)
      ECHO.    to
      ECHO.  }
      ECHO.}
    )> "_skinny_assembly_settings.sbt"
  )
  RMDIR standalone-build /S /q
  MKDIR standalone-build
  XCOPY src\* standalone-build\src\* /E /D /q
  XCOPY build.sbt standalone-build\ /q
  xcopy _skinny_assembly_settings.sbt standalone-build\ /q
  RMDIR task\src\main\resources /S /q
  RMDIR task\target /S /q
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
  RMDIR task\target /S /q
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
ECHO   new                : will create new Skinny application
ECHO   upgrade            : will upgrade Skinny app project
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
ECHO.
ECHO   eclipse       : will setup Scala IDE settings
ECHO   idea/gen-idea : will setup IntelliJ IDEA settings
ECHO.
ECHO   task:clean    : will clean task project's target directory
ECHO   task:run      : will run tasks
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
  ECHO resolvers += "scala-js-release" at "http://dl.bintray.com/scala-js/scala-js-releases" > "project\_skinny_scalajs.sbt"
  ECHO addSbtPlugin^("org.scala-js" %% "sbt-scalajs" %% "0.6.3"^) >> "project\_skinny_scalajs.sbt"

  ECHO lazy val scalajs = ^(project in file^("src/main/webapp/WEB-INF/assets"^)^).settings^( > "_skinny_scalajs_settings.sbt"
  ECHO   name := "application", // JavaScript file name  >> "_skinny_scalajs_settings.sbt"
  ECHO   scalaVersion := "2.11.6", >> "_skinny_scalajs_settings.sbt"
  ECHO   unmanagedSourceDirectories in Compile ^<+= baseDirectory^(_ / "scala"^), >> "_skinny_scalajs_settings.sbt"
  ECHO   libraryDependencies ++= Seq^(                   >> "_skinny_scalajs_settings.sbt"
  ECHO     "org.scala-js" %%%%%% "scalajs-dom"     %% "0.8.0", >> "_skinny_scalajs_settings.sbt"
  ECHO     "be.doeraene"  %%%%%% "scalajs-jquery"  %% "0.8.0", >> "_skinny_scalajs_settings.sbt"
  ECHO     "org.monifu"   %%%%  "minitest"        %% "0.11" %% "test" >> "_skinny_scalajs_settings.sbt"
  ECHO   ^), >> "_skinny_scalajs_settings.sbt"
  ECHO   crossTarget in Compile ^<^<= baseDirectory^(_ / ".." / ".." / "assets" / "js"^) >> "_skinny_scalajs_settings.sbt"
  ECHO ^).enablePlugins^(ScalaJSPlugin^) >> "_skinny_scalajs_settings.sbt"
)
sbt "project scalajs" %SUB_COMMAND%
GOTO script_eof


:script_eof


