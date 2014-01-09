import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyAppBuild extends Build {

  val skinnyVersion = "0.9.24"
  val _scalaVersion = "2.10.3"

  // In some cases, Jety 9.1 looks very slow (didn't investigate the reason)
  //val jettyVersion = "9.1.0.v20131115"
  val jettyVersion = "9.0.7.v20131107"

  val _resolovers = Seq(
    "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases"
    //,"sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
  )
  val _dependencies = Seq(
    "org.skinny-framework" %% "skinny-framework"   % skinnyVersion,
    "org.skinny-framework" %% "skinny-assets"      % skinnyVersion,
    "org.skinny-framework" %% "skinny-task"        % skinnyVersion,
    "com.h2database"       %  "h2"                 % "1.3.174", // your JDBC driver
    "ch.qos.logback"       %  "logback-classic"    % "1.0.13",
    "org.skinny-framework" %% "skinny-test"        % skinnyVersion         % "test"
  )
  val containerDependencies = Seq(
    "org.eclipse.jetty"  %  "jetty-webapp"       % jettyVersion          % "container",
    "org.eclipse.jetty"  %  "jetty-plus"         % jettyVersion          % "container",
    "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
  )

  lazy val dev = Project(id = "dev", base = file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ Seq(
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies ++ containerDependencies,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") },
      // Scalatra tests become slower when multiple controller tests are loaded in the same time
      parallelExecution in Test := false
    )
  )

  lazy val task = Project(id = "task", base = file("task"),
    settings = Defaults.defaultSettings ++ Seq(
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies,
      mainClass := Some("TaskRunner")
    )
  )

  lazy val build = Project(id = "build", base = file("build"),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := "org.skinny-framework",
      name := "skinny-blank-app",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies ++ containerDependencies,
      publishTo <<= version { (v: String) =>
        val base = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
        else Some("releases" at base + "service/local/staging/deploy/maven2")
      },
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq( TemplateConfig(file(".") / "src" / "main" / "webapp" / "WEB-INF",  Nil,  Nil,  Some("templates")))
      }
    )
  )

}

