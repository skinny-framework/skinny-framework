import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import scala.language.postfixOps

object SkinnyAppBuild extends Build {

  val skinnyVersion = "1.0.0-RC9"
  val scalatraVersion = "2.2.2"

  // In some cases, Jety 9.1 looks very slow (didn't investigate the reason)
  //val jettyVersion = "9.1.0.v20131115"
  val jettyVersion = "9.0.7.v20131107"

  lazy val baseSettings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ herokuSettings ++ Seq(
    organization := "org.skinny-framework",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.3",
    libraryDependencies := Seq(
      "org.skinny-framework"    %% "skinny-framework"   % skinnyVersion,
      "org.skinny-framework"    %% "skinny-assets"      % skinnyVersion,
      "org.skinny-framework"    %% "skinny-task"        % skinnyVersion,
      "com.h2database"          %  "h2"                 % "1.3.175",      // your own JDBC driver
      "ch.qos.logback"          %  "logback-classic"    % "1.1.1",
      // To fix java.lang.ClassNotFoundException: scala.collection.Seq when running tests
      "org.scala-lang"          %  "scala-library"      % "2.10.3"              % "test",
      "org.skinny-framework"    %% "skinny-test"        % skinnyVersion         % "test",
      "org.scalatra"            %% "scalatra-scalatest" % scalatraVersion       % "test",
      // If you prefer specs2, we don't bother you (scaffold generator supports only scalatest)
      // "org.scalatra"            %% "scalatra-specs2"    % scalatraVersion       % "test",
      "org.eclipse.jetty"       %  "jetty-webapp"       % jettyVersion          % "container",
      "org.eclipse.jetty"       %  "jetty-plus"         % jettyVersion          % "container",
      "org.eclipse.jetty.orbit" %  "javax.servlet"      % "3.0.0.v201112011016" % "container;provided;test"
    ),
    resolvers ++= Seq(
      "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases"
      ,"sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
  )

  // -------------------------------------------------------
  // Development
  // -------------------------------------------------------

  lazy val dev = Project(id = "dev", base = file("."),
    settings = baseSettings ++ Seq(
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") },
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false
    )
  )

  // -------------------------------------------------------
  // Scala.JS Trial
  // -------------------------------------------------------
/*
  lazy val scalaJS = Project(id = "scalajs", base = file("src/main/webapp/WEB-INF/assets"),
    settings = Defaults.defaultSettings ++ Seq(
      name := "application", // JavaScript file name
      unmanagedSourceDirectories in Compile <+= baseDirectory(_ / "scala"),
      libraryDependencies ++= Seq(
        "org.scala-lang.modules.scalajs" %% "scalajs-dom"                    % "0.2",
        "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % "0.3" % "test"
      ),
      crossTarget in Compile <<= baseDirectory(_ / ".." / ".." / "assets" / "js")
    )
  )
*/

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

  lazy val packagingBaseSettings = baseSettings ++ scalateSettings ++ Seq(
    publishTo <<= version { (v: String) =>
      val base = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
      else Some("releases" at base + "service/local/staging/deploy/maven2")
    },
    scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
      Seq( TemplateConfig(file(".") / "src" / "main" / "webapp" / "WEB-INF",
      // These imports should be same as src/main/scala/templates/ScalatePackage.scala
      Seq("import controller._", "import model._"),
      Seq(Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)),
      Some("templates")))
    }
  )

  lazy val build = Project(id = "build", base = file("build"),
    settings = packagingBaseSettings ++ Seq(
      name := "skinny-blank-app"
    )
  )

  lazy val standaloneBuild = Project(id = "standalone-build", base = file("standalone-build"),
    settings = packagingBaseSettings ++ Seq(
      name := "skinny-standalone-app",
      libraryDependencies += "org.skinny-framework" %% "skinny-standalone" % skinnyVersion
    ) ++ _jettyOrbitHack
  )
  val _jettyOrbitHack = Seq(
    ivyXML := <dependencies>
      <exclude org="org.eclipse.jetty.orbit" />
    </dependencies>
  )

  // -------------------------------------------------------
  // Deployment on Heroku
  // -------------------------------------------------------
  lazy val stage = taskKey[Unit]("Dummy stage task to keep Heroku happy")

  lazy val herokuSettings = Seq(
    stage        := { "heroku/stage" ! }
  )

}

