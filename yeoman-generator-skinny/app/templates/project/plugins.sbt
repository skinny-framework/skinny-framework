// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------

// --------
// Scalatra sbt plugin
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed")
))

// scalatra-sbt depends on xsbt-web-plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed")
))

// for Scalate template compilaion
addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

// --------
// scalarifrom for code formatting
// NOTE: Disabled by default because this is confusing for beginners
//
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// --------
// scoverage for test coverage (./skinny test:coverage)
// NOTE: 
//   Disabled by default because scoverage 0.98 doesn't work with Skinny ORM
//   (https://github.com/skinny-framework/skinny-framework/issues/97)
//resolvers += Classpaths.sbtPluginReleases
//addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.5")
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

