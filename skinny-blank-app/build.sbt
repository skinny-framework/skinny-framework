import sbt._, Keys._
import org.fusesource.scalate.ScalatePlugin._, ScalateKeys._
import skinny.servlet._, ServletPlugin._, ServletKeys._

import scala.language.postfixOps

// -------------------------------------------------------
// Common Settings
// -------------------------------------------------------

val appOrganization = "org.skinny-framework"
val appName = "skinny-blank-app"
val appVersion = "0.1.0-SNAPSHOT"

val skinnyVersion = "4.0.0"
val theScalaVersion = "2.13.8"
val jettyVersion = "9.4.45.v20220203"

lazy val baseSettings = servletSettings ++ Seq(
  organization := appOrganization,
  name := appName,
  version := appVersion,
  scalaVersion := theScalaVersion,
  dependencyOverrides := Seq(
    "org.scala-lang" % "scala-library" % scalaVersion.value,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    "org.slf4j" % "slf4j-api" % "1.7.36"
  ),
  libraryDependencies ++= Seq(
    "org.skinny-framework" %% "skinny-framework" % skinnyVersion,
    "org.skinny-framework" %% "skinny-assets" % skinnyVersion,
    "org.skinny-framework" %% "skinny-task" % skinnyVersion,
    "org.skinny-framework" % "skinny-logback" % "1.0.14",
    "com.h2database" % "h2" % "1.4.200", // your own JDBC driver
    "org.skinny-framework" %% "skinny-factory-girl" % skinnyVersion % "test",
    "org.skinny-framework" %% "skinny-test" % skinnyVersion % "test",
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
    "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "container",
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test"
  ),
  // ------------------------------
  // for ./skinnny console
  initialCommands := """
import _root_.skinny._
import _root_.controller._
import _root_.model._
import _root_.org.joda.time._
import _root_.scalikejdbc._
import _root_.scalikejdbc.config._
DBSettings.initialize()
""",
  resolvers ++= Seq(
    "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    //, "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  ),
  // Faster "./skinny idea"
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  updateOptions := updateOptions.value.withCachedResolution(true),
  Test / logBuffered := false,
  Test / javaOptions ++= Seq("-Dskinny.env=test"),
  Test / fork := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

lazy val scalatePrecompileSettings = scalateSettings ++ Seq(
  Compile / scalateTemplateConfig := {
    val base = (Compile / sourceDirectory).value
    Seq(
      TemplateConfig(
        file(".") / "src" / "main" / "webapp" / "WEB-INF",
        // These imports should be same as src/main/scala/templates/ScalatePackage.scala
        Seq("import controller._", "import model._"),
        Seq(
          Binding(
            "context",
            "_root_.skinny.micro.contrib.scalate.SkinnyScalateRenderContext",
            importMembers = true,
            isImplicit = true)
        ),
        Some("templates")
      )
    )
  }
)

// -------------------------------------------------------
// Development
// -------------------------------------------------------

lazy val devBaseSettings = baseSettings ++ Seq(
  Test / unmanagedClasspath += Attributed.blank(
    baseDirectory.value / "src/main/webapp"),
  // Integration tests become slower when multiple controller tests are loaded in the same time
  Test / parallelExecution := false,
  container.Configuration / port := 8080
)
lazy val dev = (project in file("."))
  .settings(devBaseSettings)
  .settings(
    name := appName + "-dev",
    target := baseDirectory.value / "target" / "dev"
  )

// --------------------------------------------------------
// Enable this sub project when you'd like to use --precompile option
// --------------------------------------------------------
/*
lazy val precompileDev = (project in file(".")).settings(devBaseSettings, scalatePrecompileSettings).settings(
  name := appName + "-precompile-dev",
  target := baseDirectory.value / "target" / "precompile-dev"
)
 */
// -------------------------------------------------------
// Task Runner
// -------------------------------------------------------

lazy val task = (project in file("task"))
  .settings(baseSettings)
  .settings(
    name := appName + "-task",
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"
  ) dependsOn (dev % "compile->compile")

// -------------------------------------------------------
// Packaging
// -------------------------------------------------------

lazy val packagingBaseSettings = baseSettings ++ scalatePrecompileSettings ++ Seq(
  Compile / doc / sources := List(),
  publishTo := {
    val base = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at base + "content/repositories/snapshots")
    else Some("releases" at base + "service/local/staging/deploy/maven2")
  }
)
lazy val build = (project in file("build"))
  .settings(packagingBaseSettings)
  .settings(
    name := appName
  )
lazy val standaloneBuild = (project in file("standalone-build"))
  .settings(packagingBaseSettings)
  .settings(
    name := appName + "-standalone",
    libraryDependencies += "org.skinny-framework" %% "skinny-standalone" % skinnyVersion,
    ivyXML := <dependencies><exclude org="org.eclipse.jetty.orbit" /></dependencies>
  )
