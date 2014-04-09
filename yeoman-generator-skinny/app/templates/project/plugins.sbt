// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------

// --------
// Scalatra sbt plugin
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5")

// scalatra-sbt depends on xsbt-web-plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0")

// for Scalate template compilaion
addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

// --------
// scalarifrom for code formatting
// NOTE: Disabled by default because this is confusing for beginners
//
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// --------
// for IntelliJ IDEA users (./skinny idea)
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// --------
// for Eclise (Scala IDE) users (./skinny eclipse)
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

// --------
// for standalone jar packaging (./skinny package:standalone)
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

// --------
// scoverage for test coverage (./skinny test:coverage)
// NOTE: 
//   Disabled by default because scoverage 0.98 doesn't work with Skinny ORM
//   (https://github.com/skinny-framework/skinny-framework/issues/97)
//resolvers += Classpaths.sbtPluginReleases
//addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.0")

// --------
// Scala.js
// http://www.scala-js.org/
//addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.4.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

