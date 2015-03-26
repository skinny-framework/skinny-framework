// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------
resolvers += Classpaths.sbtPluginReleases

// Internally uses Eclipse Aether to resolve Maven dependencies instead of Apache Ivy
// https://github.com/sbt/sbt/releases/tag/v0.13.8
//addMavenResolverPlugin

// --------
// scalac options for sbt
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// --------
// Scalatra/Scalate sbt plugin
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed"),
  ExclusionRule(organization = "com.earldouglas")
))
// scalatra-sbt depends on xsbt-web-plugin
// TODO: scalatra-sbt 0.3.5 is incompatible with xsbt-web-plugin 1.0.0
// https://github.com/scalatra/scalatra-sbt/issues/9
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.1" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed")
))

// Scalate template files precompilation
addSbtPlugin("org.skinny-framework" % "sbt-scalate-precompiler" % "1.7.1.0")

// --------
// format Scala source code automatically
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// --------
// IntelliJ IDEA setting files generator
// If you don't need this, remove org.sbtidea.SbtIdeaPlugin._ and ideaExcludeFolders too
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// --------
// scoverage for test coverage (./skinny test:coverage)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")
// Coveralls integration - http://coveralls.io/
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0.BETA1")

// check the latest version of dependencies
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.8")
