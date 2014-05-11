import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin._
import ScalateKeys._
import scala.language.postfixOps

object SkinnyAppBuild extends Build {

  // -------------------------------------------------------
  // Common Settings
  // -------------------------------------------------------

  val appOrganization = "org.skinny-framework"
  val appName = "skinny-blank-app"
  val appVersion = "0.1.0-SNAPSHOT"

  val skinnyVersion = "1.0.14"
  val scalatraVersion = "2.2.2"
  val theScalaVersion = "2.10.3"
  val jettyVersion = "9.1.0.v20131115"
  // Your project runs on Java 6, choose Jetty 8 instead.
  //val jettyVersion = "8.1.10.v20130312"

  lazy val baseSettings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ herokuSettings ++ Seq(
    organization := appOrganization,
    name         := appName,
    version      := appVersion,
    scalaVersion := theScalaVersion,
    libraryDependencies := Seq(
      "org.skinny-framework"    %% "skinny-framework"    % skinnyVersion,
      "org.skinny-framework"    %% "skinny-assets"       % skinnyVersion,
      "org.skinny-framework"    %% "skinny-task"         % skinnyVersion,
      "com.h2database"          %  "h2"                  % "1.4.178",      // your own JDBC driver
      "ch.qos.logback"          %  "logback-classic"     % "1.1.2",
      // To fix java.lang.ClassNotFoundException: scala.collection.Seq when running tests
      "org.scala-lang"          %  "scala-library"       % theScalaVersion      % "test",
      "org.skinny-framework"    %% "skinny-factory-girl" % skinnyVersion        % "test",
      "org.skinny-framework"    %% "skinny-test"         % skinnyVersion        % "test",
      "org.scalatra"            %% "scalatra-scalatest"  % scalatraVersion      % "test",
      // If you prefer specs2, we don't bother you (scaffold generator supports only scalatest)
      //"org.scalatra"            %% "scalatra-specs2"     % scalatraVersion       % "test",
      "org.eclipse.jetty"       %  "jetty-webapp"        % jettyVersion          % "container",
      "org.eclipse.jetty"       %  "jetty-plus"          % jettyVersion          % "container",
      "org.eclipse.jetty.orbit" %  "javax.servlet"       % "3.0.0.v201112011016" % "container;provided;test",
      // To fix Scalate runtime evaluation error on Java 8 (https://gist.github.com/seratch/9680709)
      "org.scala-lang"          %  "scala-compiler"      % theScalaVersion       % "container"
    ),
    resolvers ++= Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
      //,"sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    // Faster "./skinny idea" 
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    // the name-hashing algorithm for the incremental compiler.
    incOptions := incOptions.value.withNameHashing(true),
    logBuffered in Test := false,
    javaOptions in Test ++= Seq("-Dskinny.env=test"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
  )

  lazy val scalatePrecomileSettings = baseSettings ++ scalateSettings ++ Seq(
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
      target := baseDirectory.value / "target" / "dev"
    )
  )
  lazy val precompileDev = Project(id = "precompileDev", base = file("."),
    settings = devBaseSettings ++ scalatePrecomileSettings ++ Seq(
      target := baseDirectory.value / "target" / "precompile-dev"
    )
  )

  // -------------------------------------------------------
  // Task Runner
  // -------------------------------------------------------

  lazy val task = Project(id = "task", base = file("task"),
    settings = baseSettings ++ Seq(
      mainClass := Some("TaskRunner")
    )
  )

  // -------------------------------------------------------
  // Packaging
  // -------------------------------------------------------

  lazy val packagingBaseSettings = baseSettings ++ scalateSettings ++ scalatePrecomileSettings ++ Seq(
    sources in doc in Compile := List(),
    publishTo <<= version { (v: String) =>
      val base = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
      else Some("releases" at base + "service/local/staging/deploy/maven2")
    }
  )
  lazy val build = Project(id = "build", base = file("build"),
    settings = packagingBaseSettings ++ Seq(
      name := appName
    )
  )
  lazy val standaloneBuild = Project(id = "standalone-build", base = file("standalone-build"),
    settings = packagingBaseSettings ++ Seq(
      name := appName + "-standalone",
      libraryDependencies += "org.skinny-framework" %% "skinny-standalone" % skinnyVersion
    ) ++ jettyOrbitHack
  )

  // -------------------------------------------------------
  // Deployment on Heroku
  // -------------------------------------------------------

  lazy val stage = taskKey[Unit]("Dummy stage task to keep Heroku happy")
  lazy val herokuSettings = Seq(stage := { "heroku/stage" ! })

}

