import sbt._, Keys._
import skinny.scalate.ScalatePlugin._, ScalateKeys._
import skinny.servlet._, ServletPlugin._, ServletKeys._
import org.sbtidea.SbtIdeaPlugin._

import scala.language.postfixOps

object SkinnyAppBuild extends Build {

  // -------------------------------------------------------
  // Common Settings
  // -------------------------------------------------------

  val appOrganization = "org.skinny-framework"
  val appName = "skinny-blank-app"
  val appVersion = "0.1.0-SNAPSHOT"

  val skinnyVersion = "1.3.20"
  val scalatraVersion = "2.3.1"
  val theScalaVersion = "2.11.7"
  val jettyVersion = "9.2.13.v20150730"

  lazy val baseSettings = servletSettings ++ Seq(
    organization := appOrganization,
    name         := appName,
    version      := appVersion,
    scalaVersion := theScalaVersion,
    dependencyOverrides := Set(
      "org.scala-lang" %  "scala-library"  % scalaVersion.value,
      "org.scala-lang" %  "scala-reflect"  % scalaVersion.value,
      "org.scala-lang" %  "scala-compiler" % scalaVersion.value
    ),
    libraryDependencies ++= Seq(
      "org.skinny-framework"    %% "skinny-framework"    % skinnyVersion,
      "org.skinny-framework"    %% "skinny-assets"       % skinnyVersion,
      "org.skinny-framework"    %% "skinny-task"         % skinnyVersion,
      "org.skinny-framework"    %  "skinny-logback"      % "1.0.6",
      "org.apache.commons"      %  "commons-dbcp2"       % "2.1",
      "com.h2database"          %  "h2"                  % "1.4.188",      // your own JDBC driver
      "org.skinny-framework"    %% "skinny-factory-girl" % skinnyVersion   % "test",
      "org.skinny-framework"    %% "skinny-test"         % skinnyVersion   % "test",
      "org.scalatra"            %% "scalatra-scalatest"  % scalatraVersion % "test",
      "org.eclipse.jetty"       %  "jetty-webapp"        % jettyVersion    % "container",
      "org.eclipse.jetty"       %  "jetty-plus"          % jettyVersion    % "container",
      "javax.servlet"           %  "javax.servlet-api"   % "3.1.0"         % "container;provided;test"
    ),
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      //,"sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    // Faster "./skinny idea"
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    // the name-hashing algorithm for the incremental compiler.
    incOptions := incOptions.value.withNameHashing(true),
    updateOptions := updateOptions.value.withCachedResolution(true),
    logBuffered in Test := false,
    javaOptions in Test ++= Seq("-Dskinny.env=test"),
    fork in Test := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    ideaExcludeFolders := Seq(".idea", ".idea_modules", "db", "target", "task/target", "build", "standalone-build", "node_modules")
  )

  lazy val scalatePrecompileSettings = scalateSettings ++ Seq(
    scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
      Seq( TemplateConfig(file(".") / "src" / "main" / "webapp" / "WEB-INF",
      // These imports should be same as src/main/scala/templates/ScalatePackage.scala
      Seq("import controller._", "import model._"),
      Seq(Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)),
      Some("templates")))
    }
  )

  lazy val jettyOrbitHack = Seq(ivyXML := <dependencies><exclude org="org.eclipse.jetty.orbit" /></dependencies>)

  // -------------------------------------------------------
  // Development
  // -------------------------------------------------------

  lazy val devBaseSettings = baseSettings ++ Seq(
    unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") },
    // Scalatra tests become slower when multiple controller tests are loaded in the same time
    parallelExecution in Test := false,
    port in container.Configuration := 8080
  )
  lazy val dev = Project(id = "dev", base = file("."),
    settings = devBaseSettings ++ Seq(
      name := appName + "-dev",
      target := baseDirectory.value / "target" / "dev"
    )
  )
  lazy val precompileDev = Project(id = "precompileDev", base = file("."),
    settings = devBaseSettings ++ scalatePrecompileSettings ++ Seq(
      name := appName + "-precompile-dev",
      target := baseDirectory.value / "target" / "precompile-dev",
      ideaIgnoreModule := true
    )
  )

  // -------------------------------------------------------
  // Task Runner
  // -------------------------------------------------------

  lazy val task = Project(id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      mainClass := Some("TaskRunner"),
      name := appName + "-task"
    )
  ) dependsOn(dev)

  // -------------------------------------------------------
  // Packaging
  // -------------------------------------------------------

  lazy val packagingBaseSettings = baseSettings ++ scalatePrecompileSettings ++ Seq(
    sources in doc in Compile := List(),
    publishTo <<= version { (v: String) =>
      val base = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
      else Some("releases" at base + "service/local/staging/deploy/maven2")
    }
  )
  lazy val build = Project(id = "build", base = file("build"),
    settings = packagingBaseSettings ++ Seq(
      name := appName,
      ideaIgnoreModule := true
    )
  )
  lazy val standaloneBuild = Project(id = "standalone-build", base = file("standalone-build"),
    settings = packagingBaseSettings ++ Seq(
      name := appName + "-standalone",
      libraryDependencies += "org.skinny-framework" %% "skinny-standalone" % skinnyVersion,
      ideaIgnoreModule := true
    ) ++ jettyOrbitHack
  )

}

