import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._

object SkinnyAppBuild extends Build {

  val skinnyVersion = "0.9.4"
  val scalatraVersion = "2.2.1"

  lazy val app = Project (id = "app", base = file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ Seq(
      organization := "your.organization",
      name := "skinny-app",
      version := "0.0.1-SNAPSHOT",
      scalaVersion := "2.10.3",
      resolvers ++= Seq(
        "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases"
      ),
      libraryDependencies ++= Seq(
        "com.github.seratch" %% "skinny-framework"   % skinnyVersion,
        "com.h2database"     %  "h2"                 % "1.3.173", // your JDBC driver
        "com.github.seratch" %% "skinny-test"        % skinnyVersion         % "test",
        "org.scalatra"       %% "scalatra-scalatest" % scalatraVersion       % "test",
        "ch.qos.logback"     %  "logback-classic"    % "1.0.13",
        "org.eclipse.jetty"  %  "jetty-webapp"       % "8.1.13.v20130916"    % "container",
        "org.eclipse.jetty"  %  "jetty-plus"         % "8.1.13.v20130916"    % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet"  % "3.0.0.v201112011016" % "container;provided;test"
      ), 
      unmanagedClasspath in Test <+= (baseDirectory) map { bd =>  Attributed.blank(bd / "src/main/webapp") }
    )
  )

}

