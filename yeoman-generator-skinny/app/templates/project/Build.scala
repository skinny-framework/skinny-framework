import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyAppBuild extends Build {

  val skinnyVersion = "0.9.9-SNAPSHOT"
  val scalatraVersion = "2.2.1"

  val dependencies = Seq(
    "com.github.seratch" %% "skinny-framework"   % skinnyVersion,
    "com.github.seratch" %% "skinny-assets"      % skinnyVersion,
    "com.h2database"     %  "h2"                 % "1.3.173", // your JDBC driver
    "ch.qos.logback"     %  "logback-classic"    % "1.0.13",
    "com.github.seratch" %% "skinny-test"        % skinnyVersion         % "test",
    "org.scalatra"       %% "scalatra-scalatest" % scalatraVersion       % "test",
    "org.eclipse.jetty"  %  "jetty-webapp"       % "8.1.13.v20130916"    % "container",
    "org.eclipse.jetty"  %  "jetty-plus"         % "8.1.13.v20130916"    % "container",
    "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
  )

  lazy val dev = Project(id = "dev", base = file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ Seq(
      organization := "com.github.seratch",
      name := "skinny-blank-app-dev",
      version := "0.0.0",
      scalaVersion := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= dependencies,
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") }
    )
  )

  lazy val task = Project(id = "task", base = file("task"),
    settings = Defaults.defaultSettings ++ Seq(
      scalaVersion := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= Seq(
        "com.github.seratch" %% "skinny-task"   % skinnyVersion,
        "com.github.seratch" %% "skinny-assets" % skinnyVersion
      ),
      mainClass := Some("TaskLauncher")
    )
  )

  lazy val build = Project(id = "build", base = file("build"),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := "com.github.seratch",
      name := "skinny-blank-app",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases",
        "sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots"
      ),
      libraryDependencies ++= dependencies,
      publishTo <<= version { (v: String) =>
        val base = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at base + "content/repositories/snapshots")
        else Some("releases" at base + "service/local/staging/deploy/maven2")
      }
    )
  )

}

