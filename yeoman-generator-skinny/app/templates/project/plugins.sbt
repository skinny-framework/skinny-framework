// sbt plugins for this project

// --------
// Scalatra
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.4")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

// --------
// scalarifrom (disabled by default for beginners)
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.1")

// --------
// for IntelliJ IDEA users (./skinny idea)
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// --------
// for Eclise (Scala IDE) users (./skinny eclipse)
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

// --------
// for ./skinny package:standalone
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.2")

// --------
// for test coverage
// https://github.com/scoverage/sbt-scoverage
resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-scoverage" % "0.95.7")
