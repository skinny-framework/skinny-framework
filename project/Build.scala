import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyFrameworkBuild extends Build {

  val _organization = "org.skinny-framework"
  val _version = "1.1.0-SNPSHOT"
  val scalatraVersion = "2.3.0.RC3"
  val json4SVersion = "3.2.10"
  val scalikeJDBCVersion = "2.0.0"
  val h2Version = "1.4.178"
  val jettyVersion = "9.2.0.v20140526"

  lazy val baseSettings = Seq(
    // Defaults.defaultSettings is deprecated since sbt 0.13.5
    // Defaults.defaultSettings ++ Seq(
    organization := _organization,
    version := _version,
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      , "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishMavenStyle := true,
    sbtPlugin := false,
    scalacOptions ++= _scalacOptions,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
    logBuffered in Test := false,
    javaOptions in Test ++= Seq("-Dskinny.env=test"),
    pomExtra := _pomExtra
  )

  lazy val common = Project (id = "common", base = file("common"),
   settings = baseSettings ++ Seq(
      name := "skinny-common",
      libraryDependencies  <++= (scalaVersion) { scalaVersion => 
        Seq("com.typesafe" %  "config" % "1.2.1" % "compile")  ++
        jodaDependencies ++ slf4jApiDependencies ++ testDependencies ++ (scalaVersion match {
          case v if v.startsWith("2.11.") => Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1" % "compile")
          case _ => Nil
        })
      }
    ) ++ _jettyOrbitHack
  ) 

  lazy val httpClient = Project (id = "httpClient", base = file("http-client"),
   settings = baseSettings ++ Seq(
      name := "skinny-http-client",
      libraryDependencies ++= Seq(
        "org.specs2"         %% "specs2"             % "2.3.12"           % "test",
        "commons-fileupload" %  "commons-fileupload" % "1.3.1"            % "test",
        "commons-io"         %  "commons-io"         % "2.4"              % "test",
        "commons-httpclient" %  "commons-httpclient" % "3.1"              % "test",
        "javax.servlet"      %  "javax.servlet-api"  % "3.1.0"            % "test",
        "org.eclipse.jetty"  %  "jetty-server"       % jettyVersion       % "test",
        "org.eclipse.jetty"  %  "jetty-servlet"      % jettyVersion       % "test"
      ) ++ slf4jApiDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(common)

  lazy val framework = Project (id = "framework", base = file("framework"),
   settings = baseSettings ++ Seq(
      name := "skinny-framework",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(common, validator, orm, mailer, httpClient)

  lazy val standalone = Project (id = "standalone", base = file("standalone"),
    settings = baseSettings ++ Seq(
      name := "skinny-standalone",
      libraryDependencies ++= Seq(
        "javax.servlet"     %  "javax.servlet-api" % "3.1.0"       % "compile",
        "org.eclipse.jetty" %  "jetty-webapp"      % jettyVersion  % "compile",
        "org.eclipse.jetty" %  "jetty-servlet"     % jettyVersion  % "compile",
        "org.eclipse.jetty" %  "jetty-server"      % jettyVersion  % "compile"
      )
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val assets = Project (id = "assets", base = file("assets"),
    settings = baseSettings ++ Seq(
      name := "skinny-assets",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "ro.isdc.wro4j" %  "rhino"      % "1.7R5-20130223-1",
        "commons-io"    %  "commons-io" % "2.4"
      ) ++ testDependencies
    )
  ) dependsOn(framework)

  lazy val task = Project (id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      name := "skinny-task",
      libraryDependencies <++= (scalaVersion) { scalaVersion => 
        scalatraDependencies ++ Seq(
          "commons-io"             %  "commons-io" % "2.4",
          scalaVersion match { 
            case v if v.startsWith("2.11.") => "org.scalatra.scalate" %% "scalamd"    % "1.6.1" 
            case _ => "org.fusesource.scalamd" %% "scalamd"    % "1.6" 
          }
        ) ++ testDependencies
      }
    )
  ) dependsOn(assets, orm)

  lazy val orm = Project (id = "orm", base = file("orm"), 
    settings = baseSettings ++ Seq(
      name := "skinny-orm",
      libraryDependencies ++= scalikejdbcDependencies ++ servletApiDependencies ++ Seq(
        "com.googlecode.flyway" %  "flyway-core"       % "2.3.1"        % "compile",
        "org.hibernate"         %  "hibernate-core"    % "4.3.5.Final"  % "test"
      ) ++ testDependencies
    )
  ) dependsOn(common)

  lazy val factoryGirl = Project (id = "factoryGirl", base = file("factory-girl"),
    settings = baseSettings ++ Seq(
      name := "skinny-factory-girl",
      libraryDependencies ++= scalikejdbcDependencies ++ Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value
      ) ++ testDependencies
    )
  ) dependsOn(common, orm)

  lazy val freemarker = Project (id = "freemarker", base = file("freemarker"),
    settings = baseSettings ++ Seq(
      name := "skinny-freemarker",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-beanutils" %  "commons-beanutils"  % "1.9.1"   % "compile",
        "org.freemarker"    %  "freemarker"         % "2.3.20"  % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val thymeleaf = Project (id = "thymeleaf", base = file("thymeleaf"),
    settings = baseSettings ++ Seq(
      name := "skinny-thymeleaf",
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.thymeleaf"            %  "thymeleaf"                % "2.1.3.RELEASE" % "compile",
        "nz.net.ultraq.thymeleaf"  %  "thymeleaf-layout-dialect" % "1.2.4"         % "compile",
        "net.sourceforge.nekohtml" %  "nekohtml"                 % "1.9.21"        % "compile"
      ) ++ testDependencies
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val validator = Project (id = "validator", base = file("validator"),
    settings = baseSettings ++ Seq(
      name := "skinny-validator",
      libraryDependencies ++= jodaDependencies ++ testDependencies
    )
  ) dependsOn(common)

  lazy val mailer = Project ( id = "mailer", base = file("mailer"),
    settings = baseSettings ++ Seq(
      name := "skinny-mailer",
      libraryDependencies ++= mailDependencies ++ testDependencies
    )
  ) dependsOn(common)

  lazy val test = Project (id = "test", base = file("test"),
   settings = baseSettings ++ Seq(
      name := "skinny-test",
      libraryDependencies ++= scalatraDependencies ++ mailDependencies ++ testDependencies ++ Seq(
        "org.mockito"     %  "mockito-core"       % "1.9.5"            % "compile",
        "org.scalikejdbc" %% "scalikejdbc-test"   % scalikeJDBCVersion % "compile",
        "org.scalatra"    %% "scalatra-specs2"    % scalatraVersion    % "provided",
        "org.scalatra"    %% "scalatra-scalatest" % scalatraVersion    % "provided"
      )
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val example = Project (id = "example", base = file("example"),
    settings = baseSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      name := "skinny-framework-example",
      libraryDependencies ++= Seq(
        "com.h2database"     %  "h2"                 % h2Version,
        "ch.qos.logback"     % "logback-classic"     % "1.1.2",
        "org.scalatra"       %% "scalatra-specs2"    % scalatraVersion       % "test",
        "org.scalatra"       %% "scalatra-scalatest" % scalatraVersion       % "test",
        "org.mockito"        %  "mockito-core"       % "1.9.5"               % "test",
        "org.eclipse.jetty"  % "jetty-webapp"        % jettyVersion          % "container",
        "org.eclipse.jetty"  % "jetty-plus"          % jettyVersion          % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
           artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      mainClass := Some("TaskLauncher"),
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") } 
    ) 
  ) dependsOn(framework, assets, thymeleaf, freemarker, factoryGirl, test, task)

  val servletApiDependencies = Seq("javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided")
  val slf4jApiDependencies   = Seq("org.slf4j"     % "slf4j-api"         % "1.7.7" % "compile")
  val scalatraDependencies   = Seq(
    "org.scalatra"  %% "scalatra"           % scalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-scalate"   % scalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-json"      % scalatraVersion  % "compile",
    "org.json4s"    %% "json4s-jackson"     % json4SVersion    % "compile",
    "org.json4s"    %% "json4s-ext"         % json4SVersion    % "compile",
    "org.scalatra"  %% "scalatra-scalatest" % scalatraVersion  % "test"    
  ) ++ servletApiDependencies ++ slf4jApiDependencies

  val scalikejdbcDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc"               % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"), 
    "org.scalikejdbc" %% "scalikejdbc-interpolation" % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"), 
    "org.scalikejdbc" %% "scalikejdbc-config"        % scalikeJDBCVersion % "compile" exclude("org.slf4j", "slf4j-api"),
    "org.scalikejdbc" %% "scalikejdbc-test"          % scalikeJDBCVersion % "test"    
  )
  val jodaDependencies = Seq(
    "joda-time" %  "joda-time"    % "2.3"   % "compile",
    "org.joda"  %  "joda-convert" % "1.6"   % "compile"
  )
  val mailDependencies = slf4jApiDependencies ++ Seq(
    "javax.mail"              %  "mail"               % "1.4.7"          % "compile",
    "org.jvnet.mock-javamail" %  "mock-javamail"      % "1.9"            % "provided"
  )
  val testDependencies = Seq(
    "org.scalatest"           %% "scalatest"       % "2.1.7"   % "test",
    "ch.qos.logback"          %  "logback-classic" % "1.1.2"   % "test",
    "org.jvnet.mock-javamail" %  "mock-javamail"   % "1.9"     % "test",
    "com.h2database"          %  "h2"              % h2Version % "test"
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  val _scalacOptions = Seq("-deprecation", "-unchecked", "-feature")

  val _pomExtra = {
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
      </developers>
  }

  val _jettyOrbitHack = Seq(
    ivyXML := <dependencies>
      <exclude org="org.eclipse.jetty.orbit" />
    </dependencies>
  )

}
