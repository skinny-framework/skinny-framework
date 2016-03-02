import sbt._, Keys._
import skinny.scalate.ScalatePlugin._, ScalateKeys._
import skinny.servlet._, ServletPlugin._, ServletKeys._

import scala.language.postfixOps

object SkinnyFrameworkBuild extends Build {

  lazy val currentVersion = "2.0.8-SNAPSHOT"

  lazy val skinnyMicroVersion = "1.0.3"
  lazy val scalatraTestVersion = "2.4.0"
  lazy val scalikeJDBCVersion = "2.3.5"
  lazy val h2Version = "1.4.191"
  lazy val kuromojiVersion = "5.5.0"
  lazy val mockitoVersion = "1.10.19"
  // Jetty 9.3 dropped Java 7
  lazy val jettyVersion = "9.2.15.v20160210"
  lazy val logbackVersion = "1.1.6"
  lazy val slf4jApiVersion = "1.7.18"
  lazy val scalaTestVersion = "2.2.6"

  lazy val baseSettings = Seq(
    organization := "org.skinny-framework",
    version := currentVersion,
    dependencyOverrides += "org.slf4j" % "slf4j-api" % slf4jApiVersion,
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      //, "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    publishTo <<= version { (v: String) => _publishTo(v) },
    sbtPlugin := false,
    scalaVersion := "2.11.7",
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    publishMavenStyle := true,
    // NOTE: for stability
    parallelExecution in Test := false,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
    // NOTE: forking when testing doesn't work for some existing tests
    // fork in Test := true,
    logBuffered in Test := false,
    // TODO: Fix warning - javaOptions will be ignored, fork is set to false
    // javaOptions in Test ++= Seq("-Dskinny.env=test"),
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
        jodaDependencies ++ testDependencies ++ Seq(
          "org.skinny-framework" %% "skinny-micro-common"       % skinnyMicroVersion % Compile,
          // NOTE: 1.3.0 requires Java 8 or higher
          "com.typesafe"         %  "config"                    % "1.2.1"            % Compile,
          "org.apache.lucene"    %  "lucene-core"               % kuromojiVersion    % Provided,
          "org.apache.lucene"    %  "lucene-analyzers-common"   % kuromojiVersion    % Provided,
          "org.apache.lucene"    %  "lucene-analyzers-kuromoji" % kuromojiVersion    % Provided
        ) ++ (sv match {
          case v if v.startsWith("2.11.") => Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4" % Compile)
          case _ => Nil
        })
      }
    )
  ) 

  lazy val httpClient = Project(id = "httpClient", base = file("http-client"),
    settings = baseSettings ++ Seq(
      name := "skinny-http-client",
      libraryDependencies ++= Seq(
        "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion % Compile,
        "org.specs2"           %% "specs2-core"         % "2.5"              % Test,
        "commons-fileupload"   %  "commons-fileupload"  % "1.3.1"            % Test,
        "commons-io"           %  "commons-io"          % "2.4"              % Test,
        "commons-httpclient"   %  "commons-httpclient"  % "3.1"              % Test,
        "javax.servlet"        %  "javax.servlet-api"   % "3.0.1"            % Test,
        "org.eclipse.jetty"    %  "jetty-server"        % jettyVersion       % Test,
        "org.eclipse.jetty"    %  "jetty-servlet"       % jettyVersion       % Test
      ) ++ slf4jApiDependencies ++ testDependencies
    )
  )

  lazy val framework = Project(id = "framework", base = file("framework"),
    settings = baseSettings ++ Seq(
      name := "skinny-framework",
      libraryDependencies <++= (scalaVersion) { (sv) =>
        Seq(
          "org.skinny-framework" %% "skinny-micro"         % skinnyMicroVersion  % Compile,
          "org.skinny-framework" %% "skinny-micro-scalate" % skinnyMicroVersion  % Compile,
          "commons-io"           %  "commons-io"           % "2.4"               % Compile,
          "org.scalatra"         %% "scalatra-specs2"      % scalatraTestVersion % Test,
          "org.scalatra"         %% "scalatra-scalatest"   % scalatraTestVersion % Test
        ) ++ compileScalateDependencies(sv) ++ servletApiDependencies ++ testDependencies
      }
    )
  ).dependsOn(
    common,
    json,
    validator,
    orm,
    mailer,
    httpClient,
    worker
  )

  lazy val worker = Project(id = "worker", base = file("worker"),
    settings = baseSettings ++ Seq(
      name := "skinny-worker",
      libraryDependencies ++= jodaDependencies ++ testDependencies ++ Seq(
        "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion % Compile
      )
    )
  )

  // just keeping compatibility with 1.x
  lazy val standalone = Project(id = "standalone", base = file("standalone"),
    settings = baseSettings ++ Seq(
      name := "skinny-standalone",
      libraryDependencies += "org.skinny-framework" %% "skinny-micro-server" % skinnyMicroVersion % Compile
    )
  )

  lazy val assets = Project(id = "assets", base = file("assets"),
    settings = baseSettings ++ Seq(
      name := "skinny-assets",
      libraryDependencies ++= Seq(
        "ro.isdc.wro4j" %  "rhino"      % "1.7R5-20130223-1",
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ servletApiDependencies ++ testDependencies
    )
  ).dependsOn(
    framework,
    test % Test
  )

  lazy val task = Project(id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      name := "skinny-task",
      libraryDependencies ++= Seq(
        "commons-io"           %  "commons-io"          % "2.4"              % Compile,
        "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion % Compile
      ) ++ testDependencies
    )
  ).dependsOn(orm % "provided->compile")

  lazy val orm = Project(id = "orm", base = file("orm"), 
    settings = baseSettings ++ Seq(
      name := "skinny-orm",
      libraryDependencies ++= scalikejdbcDependencies ++ servletApiDependencies ++ Seq(
        "org.flywaydb"  %  "flyway-core"    % "4.0"         % Compile,
        "org.hibernate" %  "hibernate-core" % "5.0.8.Final" % Test
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
  ).dependsOn(orm)

  lazy val freemarker = Project(id = "freemarker", base = file("freemarker"),
    settings = baseSettings ++ Seq(
      name := "skinny-freemarker",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "commons-beanutils"    %  "commons-beanutils"  % "1.9.2"             % Compile,
        "org.freemarker"       %  "freemarker"         % "2.3.23"            % Compile,
        "org.skinny-framework" %% "skinny-micro-test"  % skinnyMicroVersion  % Test
      ) ++ testDependencies
    )
  ).dependsOn(framework)

  lazy val thymeleaf = Project(id = "thymeleaf", base = file("thymeleaf"),
    settings = baseSettings ++ Seq(
      name := "skinny-thymeleaf",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "org.thymeleaf"            %  "thymeleaf"                % "2.1.4.RELEASE"     % Compile,
        "nz.net.ultraq.thymeleaf"  %  "thymeleaf-layout-dialect" % "1.3.3"             % Compile exclude("org.thymeleaf", "thymeleaf"),
        "net.sourceforge.nekohtml" %  "nekohtml"                 % "1.9.22"            % Compile,
        "org.skinny-framework"     %% "skinny-micro-test"        % skinnyMicroVersion  % Test
      ) ++ testDependencies
    )
  ).dependsOn(framework)

  lazy val velocity = Project(id = "velocity", base = file("velocity"),
    settings = baseSettings ++ Seq(
      name := "skinny-velocity",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "commons-logging"     % "commons-logging" % "1.2"   % Compile,
        "org.apache.velocity" % "velocity"        % "1.7"   % Compile,
        "org.apache.velocity" % "velocity-tools"  % "2.0"   % Compile excludeAll(
          ExclusionRule("org.apache.velocity", "velocity"),
          ExclusionRule("commons-loggin", "commons-logging")
        ),
        "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion % Test
      ) ++ testDependencies 
    )
  ).dependsOn(framework)

  lazy val scaldi = Project(id = "scaldi", base = file("scaldi"),
    settings = baseSettings ++ Seq(
      name := "skinny-scaldi",
      libraryDependencies <++= (scalaVersion) { (sv) =>
        servletApiDependencies ++ Seq(
          sv match { 
            case v if v.startsWith("2.10.") => "org.scaldi" %% "scaldi" % "0.3.2"
            case _ =>                          "org.scaldi" %% "scaldi" % "0.5.7"
          },
          "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion % Test
        ) ++ testDependencies
      }
    )
  ).dependsOn(framework)

  // just keeping compatibility with 1.x
  lazy val json = Project(id = "json", base = file("json"),
    settings = baseSettings ++ Seq(
      name := "skinny-json",
      libraryDependencies ++= Seq(
        "org.skinny-framework" %% "skinny-micro-json4s" % skinnyMicroVersion % Compile
      ) ++ testDependencies
    )
  )

  lazy val oauth2 = Project(id = "oauth2", base = file("oauth2"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2",
      libraryDependencies ++= Seq(
        "org.skinny-framework"   %% "skinny-micro-common"           % skinnyMicroVersion % Compile,
        "org.apache.oltu.oauth2" %  "org.apache.oltu.oauth2.client" % "1.0.1"            % Compile exclude("org.slf4j", "slf4j-api")
      ) ++ servletApiDependencies ++ testDependencies
    )
  ).dependsOn(json)

  lazy val oauth2Controller = Project(id = "oauth2Controller", base = file("oauth2-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2-controller",
      libraryDependencies ++= servletApiDependencies ++ Seq(
        "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion % Test
      ) ++ testDependencies
    )
  ).dependsOn(framework, oauth2)

  lazy val twitterController = Project(id = "twitterController", base = file("twitter-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-twitter-controller",
      libraryDependencies ++= Seq(
        "org.twitter4j"        %  "twitter4j-core"    % "4.0.4"            % Compile,
        "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion % Test
      ) ++ servletApiDependencies ++ testDependencies
    )
  ).dependsOn(framework)

  lazy val validator = Project(id = "validator", base = file("validator"),
    settings = baseSettings ++ Seq(
      name := "skinny-validator",
      libraryDependencies ++= jodaDependencies ++ testDependencies
    )
  ).dependsOn(common)

  lazy val mailer = Project( id = "mailer", base = file("mailer"),
    settings = baseSettings ++ Seq(
      name := "skinny-mailer",
      libraryDependencies ++= mailDependencies ++ jodaDependencies ++ testDependencies
    )
  ).dependsOn(common)

  lazy val test = Project(id = "test", base = file("test"),
   settings = baseSettings ++ Seq(
      name := "skinny-test",
      libraryDependencies ++= mailDependencies ++ testDependencies ++ Seq(
        "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion % Compile,
        "org.mockito"          %  "mockito-core"      % mockitoVersion     % Compile  exclude("org.slf4j", "slf4j-api"),
        "org.scalikejdbc"      %% "scalikejdbc-test"  % scalikeJDBCVersion % Compile  exclude("org.slf4j", "slf4j-api")
      )
    )
  ).dependsOn(framework)

  // -----------------------------
  // example and tests with a real project
  
  lazy val example = Project(id = "example", base = file("example"),
    settings = baseSettings ++ servletSettings ++ scalateSettings ++ Seq(
      name := "skinny-framework-example",
      libraryDependencies ++= Seq(
        "com.h2database"       %  "h2"                 % h2Version,
        "org.skinny-framework" %  "skinny-logback"     % "1.0.8",
        "org.scalatra"         %% "scalatra-specs2"    % scalatraTestVersion % Test,
        "org.scalatra"         %% "scalatra-scalatest" % scalatraTestVersion % Test,
        "org.mockito"          %  "mockito-core"       % mockitoVersion      % Test,
        "org.eclipse.jetty"    %  "jetty-webapp"       % jettyVersion        % "container",
        "org.eclipse.jetty"    %  "jetty-plus"         % jettyVersion        % "container",
        "javax.servlet"        %  "javax.servlet-api"  % "3.0.1"             % "container;provided;test"
      ),
      mainClass := Some("TaskLauncher"),
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
  def compileScalateDependencies(sv: String) = Seq(
     sv match {
       case v if v.startsWith("2.11.") => "org.scalatra.scalate"   %% "scalamd" % "1.6.1" % Compile
       case _ =>                          "org.fusesource.scalamd" %% "scalamd" % "1.6"   % Compile
     }
   ) ++ Seq("org.scalatra.scalate" %% "scalate-core" % "1.7.1" % Compile excludeAll(fullExclusionRules: _*))

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
    "joda-time"     %  "joda-time"        % "2.9.2" % Compile,
    "org.joda"      %  "joda-convert"     % "1.8.1" % Compile
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
    "org.skinny-framework"    %  "skinny-logback"  % "1.0.8"          % Test,
    "com.h2database"          %  "h2"              % h2Version        % Test
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

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

}
