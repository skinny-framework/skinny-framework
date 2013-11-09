import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyAppBuild extends Build {

  val skinnyVersion = "0.9.14"
  val scalatraVersion = "2.2.1"
  val _scalaVersion = "2.10.3"

  val _resolovers = Seq(
    "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
    "sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots"
  )
  val _dependencies = Seq(
    "com.github.seratch" %% "skinny-framework"   % skinnyVersion,
    "com.github.seratch" %% "skinny-assets"      % skinnyVersion,
    "com.github.seratch" %% "skinny-task"        % skinnyVersion,
    "com.h2database"     %  "h2"                 % "1.3.174", // your JDBC driver
    "ch.qos.logback"     %  "logback-classic"    % "1.0.13",
    "com.github.seratch" %% "skinny-test"        % skinnyVersion         % "test",
    "org.scalatra"       %% "scalatra-scalatest" % scalatraVersion       % "test"
  )
  val _jettyDependencies = Seq(
    "org.eclipse.jetty"  %  "jetty-webapp"       % "8.1.13.v20130916"    % "container",
    "org.eclipse.jetty"  %  "jetty-plus"         % "8.1.13.v20130916"    % "container",
    "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
  )

  lazy val dev = Project(id = "dev", base = file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ Seq(
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies ++ _jettyDependencies,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") }
    )
  )

  lazy val task = Project(id = "task", base = file("task"),
    settings = Defaults.defaultSettings ++ Seq(
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies,
      mainClass := Some("TaskLauncher")
    )
  )

  lazy val build = Project(id = "build", base = file("build"),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := "com.github.seratch",
      name := "skinny-blank-app",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := _scalaVersion,
      resolvers ++= _resolovers,
      libraryDependencies ++= _dependencies ++ _jettyDependencies,
      publishTo <<= version { (v: String) =>
        val base = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
        else Some("releases" at base + "service/local/staging/deploy/maven2")
      }
    )
  )

}

