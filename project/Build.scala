import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyFrameworkBuild extends Build {

  val Organization = "com.github.seratch"
  val Version = "0.9.7-SNAPSHOT"
  val ScalatraVersion = "2.2.1"
  val Json4SVersion = "3.2.5"
  val ScalikeJDBCVersion = "1.6.10"

  lazy val common = Project (id = "common", base = file("common"),
   settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-common",
      version := Version,
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "com.typesafe" %  "config"       % "1.0.2" % "compile"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    ) ++ _jettyOrbitHack
  ) 

  lazy val framework = Project (id = "framework", base = file("framework"), 
   settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-framework",
      version := Version,
      scalaVersion := "2.10.0",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= scalatraDependencies ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    ) ++ _jettyOrbitHack
  ) dependsOn(common, assets, validator, orm)

  lazy val orm = Project (id = "orm", base = file("orm"), 
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-orm",
      version := Version,
      scalaVersion := "2.10.0",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
        Resolver.url("factory_pal repository", url("http://mgonto.github.io/releases/"))(Resolver.ivyStylePatterns)
      ),
      libraryDependencies ++= scalikejdbcDependencies ++ Seq(
        "javax.servlet"  %  "javax.servlet-api" % "3.0.1"        % "provided",
        "org.hibernate"  %  "hibernate-core"    % "4.1.12.Final" % "test",
        "com.h2database" %  "h2"                % "1.3.173"      % "test",
        "ch.qos.logback" %  "logback-classic"   % "1.0.13"       % "test",
        "ar.com.gonto"   %% "factory_pal"       % "0.2.1"        % "test"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    )
  ) dependsOn(common)

  lazy val freemarker = Project (id = "freemarker", base = file("freemarker"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-freemarker",
      version := Version,
      scalaVersion := "2.10.0",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "commons-beanutils" %  "commons-beanutils"  % "1.8.3"   % "compile",
        "org.freemarker"    %  "freemarker"         % "2.3.20"  % "compile"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val thymeleaf = Project (id = "thymeleaf", base = file("thymeleaf"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-thymeleaf",
      version := Version,
      scalaVersion := "2.10.0",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= scalatraDependencies ++ Seq(
        "org.thymeleaf"             %  "thymeleaf" % "2.0.18" % "compile",
        "net.sourceforge.nekohtml"  %  "nekohtml"  % "1.9.18" % "compile"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val validator = Project (id = "validator", base = file("validator"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-validator",
      version := Version,
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "com.typesafe" %  "config"       % "1.0.2" % "compile",
        "joda-time"    %  "joda-time"    % "2.3"   % "test",
        "org.joda"     %  "joda-convert" % "1.4"   % "test"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    )
  )

  lazy val assets = Project (id = "assets", base = file("assets"),
    settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-assets",
      version := Version,
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
        "ro.isdc.wro4j" % "rhino"        % "1.7R5-20130223-1"
      ) ++ testDependencies,
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    )
  )

  lazy val test = Project (id = "test", base = file("test"),
   settings = Defaults.defaultSettings ++ Seq(
      organization := Organization,
      name := "skinny-test",
      version := Version,
      scalaVersion := "2.10.0",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= scalatraDependencies ++ testDependencies ++ Seq(
        "com.github.seratch" %% "scalikejdbc-test"   % ScalikeJDBCVersion % "compile",
        "org.scalatra"       %% "scalatra-specs2"    % ScalatraVersion    % "compile",
        "org.scalatra"       %% "scalatra-scalatest" % ScalatraVersion    % "compile"
      ),
      publishTo <<= version { (v: String) => _publishTo(v) },
      publishMavenStyle := true,
      sbtPlugin := false,
      scalacOptions ++= _scalacOptions,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := { x => false },
      pomExtra := _pomExtra
    ) ++ _jettyOrbitHack
  ) dependsOn(framework)

  lazy val example = Project (id = "example", base = file("example"),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := "skinny-framework-example",
      version := "0.0.0",
      scalaVersion := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= Seq(
        "org.scalatra"       %% "scalatra-specs2"    % ScalatraVersion % "test",
        "org.scalatra"       %% "scalatra-scalatest" % ScalatraVersion % "test",
        "com.h2database"     %  "h2"                 % "1.3.173",
        "ch.qos.logback"     % "logback-classic"     % "1.0.13",
        "org.eclipse.jetty"  % "jetty-webapp"        % "8.1.13.v20130916" % "container",
        "org.eclipse.jetty"  % "jetty-plus"          % "8.1.13.v20130916" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test" 
           artifacts (Artifact("javax.servlet", "jar", "jar"))
      )
      , unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") } 
    )
  ) dependsOn(framework, thymeleaf, test)

  val scalatraDependencies = Seq(
    "org.scalatra"  %% "scalatra"           % ScalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-scalate"   % ScalatraVersion  % "compile",
    "org.scalatra"  %% "scalatra-json"      % ScalatraVersion  % "compile",
    "org.json4s"    %% "json4s-jackson"     % Json4SVersion    % "compile",
    "org.json4s"    %% "json4s-ext"         % Json4SVersion    % "compile",
    "org.slf4j"     %  "slf4j-api"          % "1.7.5"          % "compile",
    "javax.servlet" %  "javax.servlet-api"  % "3.0.1"          % "provided",
    "org.scalatra"  %% "scalatra-scalatest" % ScalatraVersion  % "test"
  )

  val scalikejdbcDependencies = Seq(
    "com.github.seratch" %% "scalikejdbc"               % ScalikeJDBCVersion % "compile",
    "com.github.seratch" %% "scalikejdbc-interpolation" % ScalikeJDBCVersion % "compile",
    "com.github.seratch" %% "scalikejdbc-config"        % ScalikeJDBCVersion % "compile",
    "com.github.seratch" %% "scalikejdbc-test"          % ScalikeJDBCVersion % "test"
  )

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest"   % "1.9.1" % "test",
    "org.mockito"   %  "mockito-all" % "1.9.5" % "test"
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  val _scalacOptions = Seq("-deprecation", "-unchecked", "-feature")

  val _pomExtra = {
    <url>https://github.com/seratch/skinny-framework</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:seratch/skinny-framework.git</url>
        <connection>scm:git:git@github.com:seratch/skinny-framework.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhuiro Sera</name>
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
