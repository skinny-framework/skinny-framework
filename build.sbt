import sbt._, Keys._
import org.fusesource.scalate.ScalatePlugin._, ScalateKeys._
import skinny.servlet._, ServletPlugin._, ServletKeys._

import scala.language.postfixOps

lazy val currentVersion = "3.0.3"

lazy val skinnyMicroVersion = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 12 =>
      "2.0.1" // for scala-collection-compat compatibility
    case _ =>
      "2.1.0"
  }
)
lazy val scalikeJDBCVersion   = "3.3.5"
lazy val h2Version            = "1.4.199"
lazy val kuromojiVersion      = "8.2.0"
lazy val mockitoVersion       = "2.28.2"
lazy val jettyVersion         = "9.4.19.v20190610"
lazy val logbackVersion       = "1.2.3"
lazy val slf4jApiVersion      = "1.7.26"
lazy val commonsIoVersion     = "2.6"
lazy val skinnyLogbackVersion = "1.0.14"
lazy val collectionCompatVersion = Def.setting {
  if (scalaBinaryVersion.value == "2.13")
    "2.1.1"
  else
    "0.1.1"
}

lazy val baseSettings = Seq(
  organization := "org.skinny-framework",
  version := currentVersion,
  dependencyOverrides ++= Seq(
    "org.slf4j"              % "slf4j-api"  % slf4jApiVersion,
    "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  ),
  resolvers ++= Seq(
    "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    //, "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  ),
  publishTo := _publishTo(version.value),
  sbtPlugin := false,
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.13.0", "2.12.8", "2.11.12"),
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture"),
  libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion.value,
  unmanagedSourceDirectories in Compile += {
    val base = (sourceDirectory in Compile).value.getParentFile / Defaults.nameForSrc(Compile.name)
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 => base / s"scala-2.13+"
      case _                       => base / s"scala-2.13-"
    }
  },
  publishMavenStyle := true,
  // NOTE: for stability
  parallelExecution in Test := false,
  maxErrors in Test := 10,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  // NOTE: forking when testing doesn't work for some existing tests
  // fork in Test := true,
  logBuffered in Test := false,
  testForkedParallel in Test := true,
  // TODO: Fix warning - javaOptions will be ignored, fork is set to false
  // javaOptions in Test ++= Seq("-Dskinny.env=test"),
  updateOptions := updateOptions.value.withCachedResolution(true),
  suppressSbtShellNotification := true,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options"),
  javacOptions in doc := Seq("-source", "1.8"),
  pomExtra := _pomExtra
)

// -----------------------------
// skinny libraries

lazy val common = (project in file("common"))
  .settings(baseSettings)
  .settings(
    name := "skinny-common",
    libraryDependencies ++= {
      jodaDependencies ++ testDependencies(scalaVersion.value) ++ Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % {
          if (scalaVersion.value.startsWith("2.11")) "1.1.1"
          else "1.1.2"
        }                      % Compile,
        "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion.value % Compile,
        "com.typesafe"         % "config" % "1.3.4" % Compile,
        "org.apache.lucene"    % "lucene-core" % kuromojiVersion % Provided,
        "org.apache.lucene"    % "lucene-analyzers-common" % kuromojiVersion % Provided,
        "org.apache.lucene"    % "lucene-analyzers-kuromoji" % kuromojiVersion % Provided
      )
    }
  )

lazy val httpClient = (project in file("http-client"))
  .settings(baseSettings)
  .settings(
    name := "skinny-http-client",
    libraryDependencies ++= Seq(
      "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion.value % Compile,
      "commons-fileupload"   % "commons-fileupload"   % "1.4"                    % Test,
      "commons-io"           % "commons-io"           % commonsIoVersion         % Test,
      "commons-httpclient"   % "commons-httpclient"   % "3.1"                    % Test,
      "javax.servlet"        % "javax.servlet-api"    % "3.1.0"                  % Test,
      "org.eclipse.jetty"    % "jetty-server"         % jettyVersion             % Test,
      "org.eclipse.jetty"    % "jetty-servlet"        % jettyVersion             % Test
    ) ++ slf4jApiDependencies ++ testDependencies(scalaVersion.value)
  )

lazy val framework = (project in file("framework"))
  .settings(baseSettings)
  .settings(
    name := "skinny-framework",
    libraryDependencies ++= Seq(
      "org.skinny-framework" %% "skinny-micro"         % skinnyMicroVersion.value % Compile,
      "org.skinny-framework" %% "skinny-micro-scalate" % skinnyMicroVersion.value % Compile,
      "commons-io"           % "commons-io"            % commonsIoVersion         % Compile,
      "org.skinny-framework" %% "scalatra-test"        % skinnyMicroVersion.value % Test
    ) ++ compileScalateDependencies ++ servletApiDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(
    common,
    json,
    validator,
    orm,
    mailer,
    httpClient,
    worker
  )

lazy val worker = (project in file("worker"))
  .settings(baseSettings)
  .settings(
    name := "skinny-worker",
    libraryDependencies ++= jodaDependencies ++ testDependencies(scalaVersion.value) ++ Seq(
      "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion.value % Compile
    )
  )

// just keeping compatibility with 1.x
lazy val standalone = (project in file("standalone"))
  .settings(baseSettings)
  .settings(
    name := "skinny-standalone",
    libraryDependencies += "org.skinny-framework" %% "skinny-micro-server" % skinnyMicroVersion.value % Compile
  )

lazy val assets = (project in file("assets"))
  .settings(baseSettings)
  .settings(
    name := "skinny-assets",
    libraryDependencies ++= Seq(
      "ro.isdc.wro4j" % "rhino"      % "1.7R5-20130223-1",
      "commons-io"    % "commons-io" % commonsIoVersion
    ) ++ servletApiDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(
    framework,
    test % Test
  )

lazy val task = (project in file("task"))
  .settings(baseSettings)
  .settings(
    name := "skinny-task",
    libraryDependencies ++= Seq(
      "commons-io"           % "commons-io"           % commonsIoVersion         % Compile,
      "org.skinny-framework" %% "skinny-micro-common" % skinnyMicroVersion.value % Compile
    ) ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(orm % "provided->compile")

lazy val orm = (project in file("orm"))
  .settings(baseSettings)
  .settings(
    name := "skinny-orm",
    libraryDependencies ++= scalikejdbcDependencies ++ servletApiDependencies ++ Seq(
      "org.flywaydb"    % "flyway-core"            % "5.2.4"            % Compile,
      "org.hibernate"   % "hibernate-core"         % "5.4.4.Final"      % Test,
      "org.scalikejdbc" %% "scalikejdbc-joda-time" % scalikeJDBCVersion % Test
    ) ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(common)

lazy val factoryGirl = (project in file("factory-girl"))
  .settings(baseSettings)
  .settings(
    name := "skinny-factory-girl",
    libraryDependencies ++= {
      scalikejdbcDependencies ++ Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value
      ) ++ testDependencies(scalaVersion.value)
    }
  )
  .dependsOn(orm)

lazy val freemarker = (project in file("freemarker"))
  .settings(baseSettings)
  .settings(
    name := "skinny-freemarker",
    libraryDependencies ++= servletApiDependencies ++ Seq(
      "commons-beanutils"    % "commons-beanutils"  % "1.9.3"                  % Compile,
      "org.freemarker"       % "freemarker"         % "2.3.28"                 % Compile,
      "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion.value % Test
    ) ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(framework)

lazy val thymeleaf = (project in file("thymeleaf"))
  .settings(baseSettings)
  .settings(
    name := "skinny-thymeleaf",
    libraryDependencies ++= servletApiDependencies ++ Seq(
      // TODO: thymeleaf 3 support
      // see also https://github.com/ultraq/thymeleaf-layout-dialect/issues/68
      "org.thymeleaf"            % "thymeleaf"                % "2.1.6.RELEASE"          % Compile,
      "nz.net.ultraq.thymeleaf"  % "thymeleaf-layout-dialect" % "1.4.0"                  % Compile exclude ("org.thymeleaf", "thymeleaf"),
      "net.sourceforge.nekohtml" % "nekohtml"                 % "1.9.22"                 % Compile,
      "org.skinny-framework"     %% "skinny-micro-test"       % skinnyMicroVersion.value % Test
    ) ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(framework)

// just keeping compatibility with 1.x
lazy val json = (project in file("json"))
  .settings(baseSettings)
  .settings(
    name := "skinny-json",
    libraryDependencies ++= Seq(
      "org.skinny-framework" %% "skinny-micro-json4s" % skinnyMicroVersion.value % Compile
    ) ++ testDependencies(scalaVersion.value)
  )

lazy val oauth2 = (project in file("oauth2"))
  .settings(baseSettings)
  .settings(
    name := "skinny-oauth2",
    libraryDependencies ++= Seq(
      "org.skinny-framework"   %% "skinny-micro-common"          % skinnyMicroVersion.value % Compile,
      "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.2"                  % Compile exclude ("org.slf4j", "slf4j-api")
    ) ++ servletApiDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(json)

lazy val oauth2Controller = (project in file("oauth2-controller"))
  .settings(baseSettings)
  .settings(
    name := "skinny-oauth2-controller",
    libraryDependencies ++= servletApiDependencies ++ Seq(
      "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion.value % Test
    ) ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(framework, oauth2)

lazy val twitterController = (project in file("twitter-controller"))
  .settings(baseSettings)
  .settings(
    name := "skinny-twitter-controller",
    libraryDependencies ++= Seq(
      "org.twitter4j"        % "twitter4j-core"     % "4.0.7"                  % Compile,
      "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion.value % Test
    ) ++ servletApiDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(framework)

lazy val validator = (project in file("validator"))
  .settings(baseSettings)
  .settings(
    name := "skinny-validator",
    libraryDependencies ++= jodaDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(common)

lazy val mailer = (project in file("mailer"))
  .settings(baseSettings)
  .settings(
    name := "skinny-mailer",
    libraryDependencies ++= mailDependencies ++ jodaDependencies ++ testDependencies(scalaVersion.value)
  )
  .dependsOn(common)

lazy val test = (project in file("test"))
  .settings(baseSettings)
  .settings(
    name := "skinny-test",
    libraryDependencies ++= mailDependencies ++ testDependencies(scalaVersion.value) ++ Seq(
      "org.skinny-framework" %% "skinny-micro-test" % skinnyMicroVersion.value % Compile,
      "org.mockito"          % "mockito-core"       % mockitoVersion           % Compile exclude ("org.slf4j", "slf4j-api"),
      "org.scalikejdbc"      %% "scalikejdbc-test"  % scalikeJDBCVersion       % Compile exclude ("org.slf4j", "slf4j-api")
    )
  )
  .dependsOn(framework)

// -----------------------------
// example and tests with a real project

lazy val example = (project in file("example"))
  .settings(baseSettings, servletSettings, scalateSettings)
  .settings(
    name := "skinny-framework-example",
    libraryDependencies ++= Seq(
      "com.h2database"       % "h2"                % h2Version,
      "org.skinny-framework" % "skinny-logback"    % skinnyLogbackVersion,
      "org.skinny-framework" %% "scalatra-test"    % skinnyMicroVersion.value % Test,
      "org.mockito"          % "mockito-core"      % mockitoVersion % Test,
      "org.eclipse.jetty"    % "jetty-webapp"      % jettyVersion % "container",
      "org.eclipse.jetty"    % "jetty-plus"        % jettyVersion % "container",
      "javax.servlet"        % "javax.servlet-api" % "3.1.0" % "container;provided;test"
    ),
    mainClass := Some("TaskLauncher"),
    unmanagedClasspath in Test += Attributed.blank(baseDirectory.value / "src/main/webapp")
  )
  .dependsOn(
    framework,
    assets,
    thymeleaf,
    freemarker,
    factoryGirl,
    test % Test,
    task,
    oauth2Controller,
    twitterController
  )

// -----------------------------
// common dependencies

lazy val fullExclusionRules = Seq(
  ExclusionRule("org.slf4j", "slf4j-api"),
  ExclusionRule("joda-time", "joda-time"),
  ExclusionRule("org.joda", "joda-convert"),
  ExclusionRule("log4j", "log4j"),
  ExclusionRule("org.slf4j", "slf4j-log4j12")
)
lazy val compileScalateDependencies = Seq(
  "org.scalatra.scalate" %% "scalamd"      % "1.7.3" % Compile,
  "org.scalatra.scalate" %% "scalate-core" % "1.9.4" % Compile excludeAll (fullExclusionRules: _*)
)

lazy val scalikejdbcDependencies = Seq(
  "org.scalikejdbc" %% "scalikejdbc"                      % scalikeJDBCVersion % Compile excludeAll (fullExclusionRules: _*),
  "org.scalikejdbc" %% "scalikejdbc-joda-time"            % scalikeJDBCVersion % Compile excludeAll (fullExclusionRules: _*),
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikeJDBCVersion % Compile excludeAll (fullExclusionRules: _*),
  "org.scalikejdbc" %% "scalikejdbc-config"               % scalikeJDBCVersion % Compile excludeAll (fullExclusionRules: _*),
  "org.scalikejdbc" %% "scalikejdbc-test"                 % scalikeJDBCVersion % Test
)

lazy val servletApiDependencies = Seq(
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % Compile
)
lazy val slf4jApiDependencies = Seq(
  "org.slf4j" % "slf4j-api" % slf4jApiVersion % Compile
)
lazy val jodaDependencies = Seq(
  "joda-time" % "joda-time"    % "2.10.3" % Compile,
  "org.joda"  % "joda-convert" % "2.2.1"  % Compile
)
lazy val mailDependencies = slf4jApiDependencies ++ Seq(
  "javax.mail"              % "mail"          % "1.4.7" % Compile,
  "org.jvnet.mock-javamail" % "mock-javamail" % "1.9"   % Provided
)
def scalatestV(scalaV: String) = {
  "3.0.8"
}
def testDependencies(scalaV: String) = Seq(
  "org.scalatest"           %% "scalatest"      % scalatestV(scalaV)   % Test,
  "org.mockito"             % "mockito-core"    % mockitoVersion       % Test,
  "ch.qos.logback"          % "logback-classic" % logbackVersion       % Test,
  "org.jvnet.mock-javamail" % "mock-javamail"   % "1.9"                % Test,
  "com.h2database"          % "h2"              % h2Version            % Test,
  "org.skinny-framework"    % "skinny-logback"  % skinnyLogbackVersion % Test,
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
