import sbt._, Keys._
import org.scalatra.sbt._, PluginKeys._
import com.mojolly.scalate.ScalatePlugin._, ScalateKeys._
import scala.language.postfixOps

object SkinnyFrameworkBuild extends Build {

  lazy val currentVersion = "1.3.10"
  lazy val scalatraVersion = "2.3.0"
  lazy val json4SVersion = "3.2.11"
  lazy val scalikeJDBCVersion = "2.2.3"
  lazy val h2Version = "1.4.185"
  lazy val kuromojiVersion = "4.10.3"
  lazy val mockitoVersion = "1.10.19"
  lazy val jettyVersion = "9.2.1.v20140609" // latest: "9.2.7.v20150116"

  lazy val baseSettings = Seq(
    organization := "org.skinny-framework",
    version := currentVersion,
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      //, "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishMavenStyle := true,
    sbtPlugin := false,
    scalaVersion := "2.11.5",
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    scalacOptions ++= _scalacOptions,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
    logBuffered in Test := false,
    javaOptions in Test ++= Seq("-Dskinny.env=test"),
    // TODO: Test failure in velocity module when enabling CachedResolution
    // (java.lang.NoSuchMethodError: javax.servlet.ServletContext.getContextPath()Ljava/lang/String;)
    // updateOptions := updateOptions.value.withCachedResolution(true),
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-encoding", "UTF-8", "-Xlint:-options"),
    javacOptions in doc := Seq("-source", "1.7"),
    pomExtra := _pomExtra
  )

  // -----------------------------
  // skinny libraries

  lazy val common = Project(id = "common", base = file("common"),
    settings = baseSettings ++ Seq(
      name := "skinny-common",
      libraryDependencies  <++= (scalaVersion) { scalaVersion => 
        Seq(
          "com.typesafe"      % "config"                    % "1.2.1"         % "compile",
          "org.apache.lucene" % "lucene-core"               % kuromojiVersion % "provided",
          "org.apache.lucene" % "lucene-analyzers-common"   % kuromojiVersion % "provided",
          "org.apache.lucene" % "lucene-analyzers-kuromoji" % kuromojiVersion % "provided"
        ) ++
        jodaDependencies ++ slf4jApiDependencies ++ testDependencies ++ (scalaVersion match {
          case v if v.startsWith("2.11.") => Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3" % "compile")
          case _ => Nil
        })
      }
    ) ++ _jettyOrbitHack
  ) 

  lazy val httpClient = Project(id = "httpClient", base = file("http-client"),
    settings = baseSettings ++ Seq(
      name := "skinny-http-client",
      libraryDependencies ++= Seq(
        "org.specs2"         %% "specs2-core"        % "2.4.16"           % "test",
        "commons-fileupload" %  "commons-fileupload" % "1.3.1"            % "test",
        "commons-io"         %  "commons-io"         % "2.4"              % "test",
        "commons-httpclient" %  "commons-httpclient" % "3.1"              % "test",
        "javax.servlet"      %  "javax.servlet-api"  % "3.1.0"            % "test",
        "org.eclipse.jetty"  %  "jetty-server"       % jettyVersion       % "test",
        "org.eclipse.jetty"  %  "jetty-servlet"      % jettyVersion       % "test"
      ) ++ slf4jApiDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(common)

  lazy val framework = Project(id = "framework", base = file("framework"),
    settings = baseSettings ++ Seq(
      name := "skinny-framework",
      libraryDependencies <++= (scalaVersion) { scalaVersion =>
        scalatraDependencies ++ Seq(
          "commons-io"    %  "commons-io" % "2.4",
          scalaVersion match {
            case v if v.startsWith("2.11.") => "org.scalatra.scalate"   %% "scalamd" % "1.6.1"
            case _ =>                          "org.fusesource.scalamd" %% "scalamd" % "1.6"
          }
        ) ++ testDependencies
      }
    ) ++ _jettyOrbitHack
  ).dependsOn(common, json, validator, orm, mailer, httpClient, worker)

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
        "javax.servlet"     %  "javax.servlet-api" % "3.1.0"       % "compile",
        "org.eclipse.jetty" %  "jetty-webapp"      % jettyVersion  % "compile",
        "org.eclipse.jetty" %  "jetty-servlet"     % jettyVersion  % "compile",
        "org.eclipse.jetty" %  "jetty-server"      % jettyVersion  % "compile"
      )
    ) ++ _jettyOrbitHack
  ).dependsOn(framework)

  lazy val assets = Project(id = "assets", base = file("assets"),
    settings = baseSettings ++ Seq(
      name := "skinny-assets",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "ro.isdc.wro4j" %  "rhino"      % "1.7R5-20130223-1",
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    )
  ).dependsOn(framework)

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
        "org.flywaydb"    %  "flyway-core"    % "3.1"         % "compile",
        "org.hibernate"   %  "hibernate-core" % "4.3.8.Final" % "test"
      ) ++ testDependencies
    )
  ).dependsOn(common)

  lazy val factoryGirl = Project(id = "factoryGirl", base = file("factory-girl"),
    settings = baseSettings ++ Seq(
      name := "skinny-factory-girl",
      libraryDependencies ++= scalikejdbcDependencies ++ Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value
      ) ++ testDependencies
    )
  ).dependsOn(common, orm)

  lazy val freemarker = Project(id = "freemarker", base = file("freemarker"),
    settings = baseSettings ++ Seq(
      name := "skinny-freemarker",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-beanutils" %  "commons-beanutils"  % "1.9.2"   % "compile",
        "org.freemarker"    %  "freemarker"         % "2.3.21"  % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(framework)

  lazy val thymeleaf = Project(id = "thymeleaf", base = file("thymeleaf"),
    settings = baseSettings ++ Seq(
      name := "skinny-thymeleaf",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.thymeleaf"            %  "thymeleaf"                % "2.1.4.RELEASE" % "compile",
        "nz.net.ultraq.thymeleaf"  %  "thymeleaf-layout-dialect" % "1.2.7"         % "compile" exclude("org.thymeleaf", "thymeleaf"),
        "net.sourceforge.nekohtml" %  "nekohtml"                 % "1.9.21"        % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ).dependsOn(framework)

  lazy val velocity = Project(id = "velocity", base = file("velocity"),
    settings = baseSettings ++ Seq(
      name := "skinny-velocity",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.apache.velocity" % "velocity"       % "1.7" % "compile",
        "org.apache.velocity" % "velocity-tools" % "2.0" % "compile"
      ) ++ testDependencies 
    ) ++ _jettyOrbitHack
  ).dependsOn(framework)

  lazy val scaldi = Project(id = "scaldi", base = file("scaldi"),
    settings = baseSettings ++ Seq(
      name := "skinny-scaldi",
      libraryDependencies <++= (scalaVersion) { scalaVersion => scalatraDependencies ++ 
        Seq(
          scalaVersion match { 
            case v if v.startsWith("2.10.") => "org.scaldi" %% "scaldi" % "0.3.2"
            case _ =>                          "org.scaldi" %% "scaldi" % "0.4"
          }
        ) ++ testDependencies
      }
    )
  ).dependsOn(framework)

  lazy val json = Project(id = "json", base = file("json"),
    settings = baseSettings ++ Seq(
      name := "skinny-json",
      libraryDependencies ++= json4sDependencies ++ testDependencies
    )
  )

  lazy val oauth2 = Project(id = "oauth2", base = file("oauth2"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2",
      libraryDependencies ++= Seq(
        "org.apache.oltu.oauth2" %  "org.apache.oltu.oauth2.client" % "1.0.0" % "compile"
      ) ++ servletApiDependencies ++ testDependencies
    )
  ).dependsOn(common, json)

  lazy val oauth2Controller = Project(id = "oauth2Controller", base = file("oauth2-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-oauth2-controller",
      libraryDependencies ++= servletApiDependencies
    )
  ).dependsOn(framework, oauth2)

  lazy val twitterController = Project(id = "twitterController", base = file("twitter-controller"),
    settings = baseSettings ++ Seq(
      name := "skinny-twitter-controller",
      libraryDependencies ++= Seq(
        "org.twitter4j" % "twitter4j-core" % "4.0.2" % "compile"
      ) ++ servletApiDependencies
    )
  ).dependsOn(framework)

  lazy val logback = Project(id = "logback", base = file("logback"),
    settings = baseSettings ++ Seq(
      name             := "skinny-logback",
      version          := "1.0.3",
      crossPaths       := false,
      autoScalaLibrary := false,
      libraryDependencies ++= Seq("ch.qos.logback" % "logback-classic" % "1.1.2" % "compile")
    )
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
        "org.mockito"     %  "mockito-core"       % mockitoVersion     % "compile",
        "org.scalikejdbc" %% "scalikejdbc-test"   % scalikeJDBCVersion % "compile",
        "org.scalatra"    %% "scalatra-specs2"    % scalatraVersion    % "provided",
        "org.scalatra"    %% "scalatra-scalatest" % scalatraVersion    % "provided"
      )
    ) ++ _jettyOrbitHack
  ).dependsOn(framework)

  // -----------------------------
  // example and tests with a real project
  
  lazy val example = Project(id = "example", base = file("example"),
    settings = baseSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      name := "skinny-framework-example",
      libraryDependencies ++= Seq(
        "com.h2database"     %  "h2"                 % h2Version,
        "ch.qos.logback"     %  "logback-classic"    % "1.1.2",
        "org.scalatra"       %% "scalatra-specs2"    % scalatraVersion       % "test",
        "org.scalatra"       %% "scalatra-scalatest" % scalatraVersion       % "test",
        "org.mockito"        %  "mockito-core"       % mockitoVersion        % "test",
        "org.eclipse.jetty"  %  "jetty-webapp"       % jettyVersion          % "container",
        "org.eclipse.jetty"  %  "jetty-plus"         % jettyVersion          % "container",
        "javax.servlet"      %  "javax.servlet-api"  % "3.1.0"               % "container;provided;test"
      ),
      mainClass := Some("TaskLauncher"),
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") } 
    ) 
  ).dependsOn(
    framework, 
    assets, 
    logback, 
    thymeleaf, 
    freemarker, 
    velocity, 
    factoryGirl, 
    test, 
    task, 
    scaldi, 
    oauth2Controller, 
    twitterController
  )

  // -----------------------------
  // common dependencies
 
  lazy val servletApiDependencies = Seq("javax.servlet" % "javax.servlet-api" % "3.1.0"  % "provided")
  lazy val slf4jApiDependencies   = Seq("org.slf4j"     % "slf4j-api"         % "1.7.10" % "compile")
  lazy val json4sDependencies = Seq(
    "org.json4s"    %% "json4s-jackson"     % json4SVersion    % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.json4s"    %% "json4s-ext"         % json4SVersion    % "compile" exclude("org.slf4j", "slf4j-api")
  )
  lazy val scalatraDependencies   = Seq(
    "org.scalatra"  %% "scalatra"           % scalatraVersion  % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalatra"  %% "scalatra-scalate"   % scalatraVersion  % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalatra"  %% "scalatra-json"      % scalatraVersion  % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalatra"  %% "scalatra-scalatest" % scalatraVersion  % "test"    
  ) ++ json4sDependencies ++ servletApiDependencies ++ slf4jApiDependencies

  lazy val scalikejdbcDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc"                      % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"), 
    "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalikejdbc" %% "scalikejdbc-config"               % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalikejdbc" %% "scalikejdbc-test"                 % scalikeJDBCVersion % "test"
  )
  lazy val jodaDependencies = Seq(
    "joda-time" %  "joda-time"    % "2.7"   % "compile",
    "org.joda"  %  "joda-convert" % "1.7"   % "compile"
  )
  lazy val mailDependencies = slf4jApiDependencies ++ Seq(
    "javax.mail"              %  "mail"               % "1.4.7"          % "compile",
    "org.jvnet.mock-javamail" %  "mock-javamail"      % "1.9"            % "provided"
  )
  lazy val testDependencies = Seq(
    "org.scalatest"           %% "scalatest"       % "2.2.4"        % "test",
    "org.mockito"             %  "mockito-core"    % mockitoVersion % "test",
    "ch.qos.logback"          %  "logback-classic" % "1.1.2"        % "test",
    "org.jvnet.mock-javamail" %  "mock-javamail"   % "1.9"          % "test",
    "com.h2database"          %  "h2"              % h2Version      % "test",
    "org.skinny-framework"    %  "skinny-logback"  % "1.0.3"        % "test",
    "com.h2database"          %  "h2"              % h2Version      % "test"
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

  // TODO: just dummy for sbt-scoverage 0.99.x requirement
  // since 1.0, we should remove this settings def.
  // I will remove this after checking scoverage 1.0 works fine with this project
  lazy val instrumentSettings = Nil

}
