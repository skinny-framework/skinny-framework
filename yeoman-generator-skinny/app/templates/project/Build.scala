import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object SkinnyAppBuild extends Build {

  val skinnyVersion = "0.9.27"

  // In some cases, Jety 9.1 looks very slow (didn't investigate the reason)
  //val jettyVersion = "9.1.0.v20131115"
  val jettyVersion = "9.0.7.v20131107"

  lazy val baseSettings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ Seq(
    organization := "org.skinny-framework",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.3",
    libraryDependencies := Seq(
      "org.skinny-framework"    %% "skinny-framework"   % skinnyVersion,
      "org.skinny-framework"    %% "skinny-assets"      % skinnyVersion,
      "org.skinny-framework"    %% "skinny-task"        % skinnyVersion,
      "com.h2database"          %  "h2"                 % "1.3.174",      // your own JDBC driver
      "ch.qos.logback"          %  "logback-classic"    % "1.0.13",
      "org.skinny-framework"    %% "skinny-test"        % skinnyVersion         % "test",
      "org.eclipse.jetty"       %  "jetty-webapp"       % jettyVersion          % "container",
      "org.eclipse.jetty"       %  "jetty-plus"         % jettyVersion          % "container",
      "org.eclipse.jetty.orbit" %  "javax.servlet"      % "3.0.0.v201112011016" % "container;provided;test"
    ),
    resolvers ++= Seq(
      "sonatype releases"  at "http://oss.sonatype.org/content/repositories/releases"
      //,"sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
    )
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

}

