import sbt._, Keys._
import skinny.scalate.ScalatePlugin._, ScalateKeys._
import skinny.servlet._, ServletPlugin._, ServletKeys._

import scala.language.postfixOps

object SkinnyFrameworkBuild extends Build {

  lazy val currentVersion = "2.0.0-SNAPSHOT"
  // Scalatra 2.4 will be incompatible with Skinny
  lazy val compatibleScalatraVersion = "2.3.1"
  // TODO: fix version before skinny 2.0.0
  lazy val json4SVersion = "3.3.0.RC3"
  lazy val scalikeJDBCVersion = "2.2.8"
  lazy val h2Version = "1.4.188"
  lazy val kuromojiVersion = "5.2.1"
  lazy val mockitoVersion = "1.10.19"
  // Jetty 9.3 dropped Java 7
  lazy val jettyVersion = "9.2.13.v20150730"
  lazy val logbackVersion = "1.1.3"
  lazy val slf4jApiVersion = "1.7.12"
  lazy val scalaTestVersion = "2.2.5"

  lazy val baseSettings = Seq(
    organization := "org.skinny-framework",
    version := currentVersion,
    dependencyOverrides += "org.slf4j" %  "slf4j-api"  % slf4jApiVersion,
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      //,"sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishMavenStyle := true,
    sbtPlugin := false,
    scalaVersion := "2.11.7",
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    scalacOptions ++= _scalacOptions,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
    logBuffered in Test := false,
    javaOptions in Test ++= Seq("-Dskinny.env=test"),
    updateOptions := updateOptions.value.withCachedResolution(true),
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-encoding", "UTF-8", "-Xlint:-options"),
    javacOptions in doc := Seq("-source", "1.7"),
    pomExtra := _pomExtra
  )

  // -----------------------------
  // skinny libraries

  lazy val common = Project(id = "common", base = file("common"),
    settings = baseSettings ++ Seq(
      name := "skinny-common",
      libraryDependencies  <++= (scalaVersion) { (sv) =>
        Seq(
          // NOTE: 1.3.0 requires Java 8 or higher
          "com.typesafe"      % "config"                    % "1.2.1"         % Compile,
          "org.apache.lucene" % "lucene-core"               % kuromojiVersion % Provided,
          "org.apache.lucene" % "lucene-analyzers-common"   % kuromojiVersion % Provided,
          "org.apache.lucene" % "lucene-analyzers-kuromoji" % kuromojiVersion % Provided
        ) ++
        jodaDependencies ++ slf4jApiDependencies ++ testDependencies ++ (sv match {
          case v if v.startsWith("2.11.") => Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4" % Compile)
          case _ => Nil
        })
      }
    ) ++ _jettyOrbitHack
  ) 

  lazy val httpClient = Project(id = "httpClient", base = file("http-client"),
    settings = baseSettings ++ Seq(
      name := "skinny-http-client",
      libraryDependencies ++= Seq(
        "org.specs2"         %% "specs2-core"        % "2.4.17"           % Test,
        "commons-fileupload" %  "commons-fileupload" % "1.3.1"            % Test,
        "commons-io"         %  "commons-io"         % "2.4"              % Test,
        "commons-httpclient" %  "commons-httpclient" % "3.1"              % Test,
        "javax.servlet"      %  "javax.servlet-api"  % "3.0.1"            % Test,
        "org.eclipse.jetty"  %  "jetty-server"       % jettyVersion       % Test,
        "org.eclipse.jetty"  %  "jetty-servlet"      % jettyVersion       % Test
      ) ++ slf4jApiDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(common)

  lazy val framework = Project(id = "framework", base = file("framework"),
    settings = baseSettings ++ Seq(
      name := "skinny-framework",
      libraryDependencies <++= (scalaVersion) { (sv) =>
        scalatraDependencies ++ Seq(
          "commons-io"    %  "commons-io" % "2.4"
        ) ++ compileScalateDependencies(sv) ++ testDependencies ++ Seq(
          "org.scalatra"    %% "scalatra-specs2"    % compatibleScalatraVersion % Test,
          "org.scalatra"    %% "scalatra-scalatest" % compatibleScalatraVersion % Test
        )
      }
    ) ++ _jettyOrbitHack
  ).dependsOn(
    common,
    engine,
    engineJson,
    engineScalate,
    json,
    validator,
    orm,
    mailer,
    httpClient,
    worker
  )

  // a Scalatra 2.4 fork project (no CoreDsl macros)
  lazy val engine = Project(id = "engine", base = file("engine"),
    settings = baseSettings ++ Seq(
      name := "skinny-engine",
      libraryDependencies <++= (scalaVersion) { (sv) =>
        scalatraDependencies ++ Seq(
          "org.scalatra"      %% "scalatra-specs2"    % compatibleScalatraVersion % Test,
          "org.scalatra"      %% "scalatra-scalatest" % compatibleScalatraVersion % Test,
          "com.typesafe.akka" %% "akka-actor"         % "2.3.12"                  % Test
        ) ++ testDependencies
      }
    ) ++ _jettyOrbitHack
  ).dependsOn(
    common
  )

  lazy val engineJson = Project(id = "engineJson", base = file("engine-json"),
    settings = baseSettings ++ Seq(
      name := "skinny-engine-json",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "org.scalatra"      %% "scalatra-specs2"    % compatibleScalatraVersion % Test,
        "org.scalatra"      %% "scalatra-scalatest" % compatibleScalatraVersion % Test,
        "com.typesafe.akka" %% "akka-actor"         % "2.3.12"                  % Test
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(
    engine,
    json
  )

  lazy val engineScalate = Project(id = "engineScalate", base = file("engine-scalate"),
    settings = baseSettings ++ Seq(
      name := "skinny-engine-scalate",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "org.scalatra.scalate"  %% "scalate-core"       % "1.7.1" excludeAll(fullExclusionRules: _*),
        "org.scalatra"          %% "scalatra-specs2"    % compatibleScalatraVersion % Test,
        "org.scalatra"          %% "scalatra-scalatest" % compatibleScalatraVersion % Test,
        "com.typesafe.akka"     %% "akka-actor"         % "2.3.12"                  % Test
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(
    engine
  )

  lazy val engineServer = Project(id = "engineServer", base = file("engine-server"),
    settings = baseSettings ++ Seq(
      name := "skinny-engine-server",
      libraryDependencies ++= testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(
    engine,
    standalone,
    engineJson % Test,
    httpClient % Test
  )

  lazy val engineTest = Project(id = "engineTest", base = file("engine-test"),
    settings = baseSettings ++ Seq(
      name := "skinny-engine-test",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        // TODO: sbt occasionally fails to reselve junit when specifying 4.12
        "junit"              %  "junit"            % "4.11"       % Compile,
        "org.apache.commons" %  "commons-lang3"    % "3.4"        % Compile,
        "org.eclipse.jetty"  %  "jetty-webapp"     % jettyVersion % Compile,
        "org.apache.httpcomponents" % "httpclient" % "4.5"        % Compile,
        "org.apache.httpcomponents" % "httpmime"   % "4.5"        % Compile,
        "org.scalatest"      %% "scalatest"        % scalaTestVersion % Compile
      )
    )
  ).dependsOn(
    common, 
    engine
  )

  lazy val worker = Project(id = "worker", base = file("worker"),
    settings = baseSettings ++ Seq(
      name := "skinny-worker",
      libraryDependencies ++= testDependencies
    )
  ).dependsOn(common)

  lazy val standalone = Project(id = "standalone", base = file("standalone"),
    settings = baseSettings ++ Seq(
      name := "skinny-standalone",
      libraryDependencies ++= Seq(
        "javax.servlet"     %  "javax.servlet-api" % "3.0.1"       % Compile,
        "org.eclipse.jetty" %  "jetty-webapp"      % jettyVersion  % Compile,
        "org.eclipse.jetty" %  "jetty-servlet"     % jettyVersion  % Compile,
        "org.eclipse.jetty" %  "jetty-server"      % jettyVersion  % Compile
      )
    ) ++ _jettyOrbitHack
  ).dependsOn(framework % Provided)

  lazy val assets = Project(id = "assets", base = file("assets"),
    settings = baseSettings ++ Seq(
      name := "skinny-assets",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "ro.isdc.wro4j" %  "rhino"      % "1.7R5-20130223-1",
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    )
  ).dependsOn(
    framework,
    test % Test
  )

  lazy val task = Project(id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      name := "skinny-task",
      libraryDependencies ++= Seq("commons-io" %  "commons-io" % "2.4") ++ testDependencies
    )
  ).dependsOn(common, orm % "provided->compile")

  lazy val orm = Project(id = "orm", base = file("orm"), 
    settings = baseSettings ++ Seq(
      name := "skinny-orm",
      libraryDependencies ++= scalikejdbcDependencies ++ servletApiDependencies ++ Seq(
        "org.flywaydb"    %  "flyway-core"    % "3.2.1"        % Compile,
        "org.hibernate"   %  "hibernate-core" % "4.3.11.Final" % Test
      ) ++ testDependencies
    )
  ).dependsOn(common)

  lazy val factoryGirl = Project(id = "factoryGirl", base = file("factory-girl"),
    settings = baseSettings ++ Seq(
      name := "skinny-factory-girl",
      libraryDependencies <++= (scalaVersion) { (sv) =>
        scalikejdbcDependencies ++ Seq(
          "org.scala-lang" % "scala-compiler" % sv
        ) ++ testDependencies
      }
    )
  ).dependsOn(common, orm)

  lazy val freemarker = Project(id = "freemarker", base = file("freemarker"),
    settings = baseSettings ++ Seq(
      name := "skinny-freemarker",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-beanutils" %  "commons-beanutils"  % "1.9.2"   % Compile,
        "org.freemarker"    %  "freemarker"         % "2.3.23"  % Compile
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(
    framework,
    engineTest % Test
  )

  lazy val thymeleaf = Project(id = "thymeleaf", base = file("thymeleaf"),
    settings = baseSettings ++ Seq(
      name := "skinny-thymeleaf",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.thymeleaf"            %  "thymeleaf"                % "2.1.4.RELEASE" % Compile,
        "nz.net.ultraq.thymeleaf"  %  "thymeleaf-layout-dialect" % "1.2.9"         % Compile exclude("org.thymeleaf", "thymeleaf"),
        "net.sourceforge.nekohtml" %  "nekohtml"                 % "1.9.22"        % Compile
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(
    framework,
    engineTest % Test
  )

  lazy val velocity = Project(id = "velocity", base = file("velocity"),
    settings = baseSettings ++ Seq(
      name := "skinny-velocity",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-logging"     % "commons-logging" % "1.2"   % Compile,
        "org.apache.velocity" % "velocity"        % "1.7"   % Compile,
        "org.apache.velocity" % "velocity-tools"  % "2.0"   % Compile excludeAll(
          ExclusionRule("org.apache.velocity", "velocity"),
          ExclusionRule("commons-loggin", "commons-logging")
        )
      ) ++ testDependencies 
    ) ++ _jettyOrbitHack
  ).dependsOn(
    framework,
    engineTest % Test
  )

  lazy val scaldi = Project(id = "scaldi", base = file("scaldi"),
    settings = baseSettings ++ Seq(
      name := "skinny-scaldi",
      libraryDependencies <++= (scalaVersion) { (sv) => scalatraDependencies ++ 
        Seq(
          sv match { 
            case v if v.startsWith("2.10.") => "org.scaldi" %% "scaldi" % "0.3.2"
            case _ =>                          "org.scaldi" %% "scaldi" % "0.5.6"
          }
        ) ++ testDependencies
      }
    )
  ).dependsOn(
    framework,
    engineTest % Test
  )

  lazy val json = Project(id = "json", base = file("json"),
    settings = baseSettings ++ Seq(
      name := "skinny-json",
      libraryDependencies ++= json4sDependencies ++ jodaDependencies ++ testDependencies
    )
  )

  lazy val oauth2 = Project(id = "oauth2", base = file("oauth2"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2",
      libraryDependencies ++= Seq(
        "org.apache.oltu.oauth2" %  "org.apache.oltu.oauth2.client" % "1.0.0" % Compile exclude("org.slf4j", "slf4j-api")
      ) ++ servletApiDependencies ++ testDependencies
    )
  ).dependsOn(common, json)

  lazy val oauth2Controller = Project(id = "oauth2Controller", base = file("oauth2-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2-controller",
      libraryDependencies ++= servletApiDependencies
    )
  ).dependsOn(
    framework, 
    oauth2,
    engineTest % Test
  )

  lazy val twitterController = Project(id = "twitterController", base = file("twitter-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-twitter-controller",
      libraryDependencies ++= Seq(
        "org.twitter4j" % "twitter4j-core" % "4.0.3" % Compile
      ) ++ servletApiDependencies
    )
  ).dependsOn(
    framework,
    engineTest % Test
  )

  lazy val validator = Project(id = "validator", base = file("validator"),
    settings = baseSettings ++ Seq(
      name := "skinny-validator",
      libraryDependencies ++= jodaDependencies ++ testDependencies
    )
  ).dependsOn(common)

  lazy val mailer = Project( id = "mailer", base = file("mailer"),
    settings = baseSettings ++ Seq(
      name := "skinny-mailer",
      libraryDependencies ++= mailDependencies ++ testDependencies
    )
  ).dependsOn(common)

  lazy val test = Project(id = "test", base = file("test"),
   settings = baseSettings ++ Seq(
      name := "skinny-test",
      libraryDependencies ++= scalatraDependencies ++ mailDependencies ++ testDependencies ++ Seq(
        "org.mockito"     %  "mockito-core"       % mockitoVersion        % Compile  exclude("org.slf4j", "slf4j-api"),
        "org.scalikejdbc" %% "scalikejdbc-test"   % scalikeJDBCVersion    % Compile  exclude("org.slf4j", "slf4j-api")
      )
    ) ++ _jettyOrbitHack
  ).dependsOn(
    framework,
    engineTest
  )

  // -----------------------------
  // example and tests with a real project
  
  lazy val example = Project(id = "example", base = file("example"),
    settings = baseSettings ++ servletSettings ++ scalateSettings ++ Seq(
      name := "skinny-framework-example",
      libraryDependencies ++= Seq(
        "com.h2database"       %  "h2"                 % h2Version,
        "org.skinny-framework" %  "skinny-logback"     % "1.0.6",
        "org.scalatra"         %% "scalatra-specs2"    % compatibleScalatraVersion % Test,
        "org.scalatra"         %% "scalatra-scalatest" % compatibleScalatraVersion % Test,
        "org.mockito"          %  "mockito-core"       % mockitoVersion            % Test,
        "org.eclipse.jetty"    %  "jetty-webapp"       % jettyVersion              % "container",
        "org.eclipse.jetty"    %  "jetty-plus"         % jettyVersion              % "container",
        "javax.servlet"        %  "javax.servlet-api"  % "3.0.1"                   % "container;provided;test"
      ),
      mainClass := Some("TaskLauncher"),
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") } 
    ) 
  ).dependsOn(
    framework, 
    assets, 
    thymeleaf, 
    freemarker, 
    velocity, 
    factoryGirl, 
    test % Test, 
    task, 
    scaldi, 
    oauth2Controller, 
    twitterController
  )

  // -----------------------------
  // common dependencies

  lazy val fullExclusionRules = Seq(
    ExclusionRule("org.slf4j", "slf4j-api"),
    ExclusionRule("joda-time", "joda-time"),
    ExclusionRule("org.joda",  "joda-convert"),
    ExclusionRule("log4j",     "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12")
  )
  lazy val json4sDependencies = Seq(
    "org.json4s"    %% "json4s-jackson"     % json4SVersion    % Compile  excludeAll(fullExclusionRules: _*),
    "org.json4s"    %% "json4s-native"      % json4SVersion    % Provided excludeAll(fullExclusionRules: _*),
    "org.json4s"    %% "json4s-ext"         % json4SVersion    % Compile  excludeAll(fullExclusionRules: _*)
  )
  lazy val scalatraDependencies = json4sDependencies ++ servletApiDependencies ++ slf4jApiDependencies ++ Seq(
    "org.scalatra.rl"                  %% "rl"                % "0.4.10"  % Compile,
    "com.googlecode.juniversalchardet" %  "juniversalchardet" % "1.0.3"   % Compile
  )
  def compileScalateDependencies(sv: String) = Seq(
     sv match {
       case v if v.startsWith("2.11.") => "org.scalatra.scalate"   %% "scalamd" % "1.6.1" % Compile
       case _ =>                          "org.fusesource.scalamd" %% "scalamd" % "1.6"   % Compile
     }
   ) ++ Seq("org.scalatra.scalate"             %% "scalate-core"      % "1.7.1"   % Compile excludeAll(fullExclusionRules: _*))

  lazy val scalikejdbcDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc"                      % scalikeJDBCVersion % Compile excludeAll(fullExclusionRules: _*),
    "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikeJDBCVersion % Compile excludeAll(fullExclusionRules: _*),
    "org.scalikejdbc" %% "scalikejdbc-config"               % scalikeJDBCVersion % Compile excludeAll(fullExclusionRules: _*),
    "org.scalikejdbc" %% "scalikejdbc-test"                 % scalikeJDBCVersion % Test
  )

  lazy val servletApiDependencies = Seq(
    "javax.servlet" % "javax.servlet-api" % "3.0.1"  % Provided
  )
  lazy val slf4jApiDependencies   = Seq(
    "org.slf4j"     % "slf4j-api"         % slf4jApiVersion % Compile
  )
  lazy val jodaDependencies = Seq(
    "joda-time"     %  "joda-time"        % "2.8.1"  % Compile,
    "org.joda"      %  "joda-convert"     % "1.7"    % Compile
  )
  lazy val mailDependencies = slf4jApiDependencies ++ Seq(
    "javax.mail"              %  "mail"            % "1.4.7"          % Compile,
    "org.jvnet.mock-javamail" %  "mock-javamail"   % "1.9"            % Provided
  )
  lazy val testDependencies = Seq(
    "org.scalatest"           %% "scalatest"       % scalaTestVersion % Test,
    "org.mockito"             %  "mockito-core"    % mockitoVersion   % Test,
    "ch.qos.logback"          %  "logback-classic" % logbackVersion   % Test,
    "org.jvnet.mock-javamail" %  "mock-javamail"   % "1.9"            % Test,
    "com.h2database"          %  "h2"              % h2Version        % Test,
    "org.skinny-framework"    %  "skinny-logback"  % "1.0.6"          % Test,
    "com.h2database"          %  "h2"              % h2Version        % Test
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  lazy val _scalacOptions = Seq("-deprecation", "-unchecked", "-feature")

  lazy val _pomExtra = {
    <url>http://skinny-framework.org/</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:skinny-framework/skinny-framework.git</url>
        <connection>scm:git:git@github.com:skinny-framework/skinny-framework.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
        <developer>
          <id>namutaka</id>
          <name>namu</name>
          <url>https://github.com/namutaka</url>
        </developer>
        <developer>
          <id>Arakaki</id>
          <name>Yusuke Arakaki</name>
          <url>https://github.com/Arakaki</url>
        </developer>
        <developer>
          <id>cb372</id>
          <name>Chris Birchall</name>
          <url>https://github.com/cb372</url>
        </developer>
        <developer>
          <id>argius</id>
          <name>argius</name>
          <url>https://github.com/argius</url>
        </developer>
        <developer>
          <id>gakuzzzz</id>
          <name>Manabu Nakamura</name>
          <url>https://github.com/gakuzzzz</url>
        </developer>
        <developer>
          <id>BlackPrincess</id>
          <name>BlackPrincess</name>
          <url>https://github.com/BlackPrincess</url>
        </developer>
      </developers>
  }

  lazy val _jettyOrbitHack = Seq(
    ivyXML := <dependencies>
      <exclude org="org.eclipse.jetty.orbit" />
    </dependencies>
  )

}
