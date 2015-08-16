// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------
resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"

// Internally uses Eclipse Aether to resolve Maven dependencies instead of Apache Ivy
// https://github.com/sbt/sbt/releases/tag/v0.13.8
//addMavenResolverPlugin

// --------
// scalac options for sbt
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// --------
// Servlet app packager/runner plugin
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin" % "2.0.1")

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
// NOTICE: 1.2.0 looks unstable
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")
// Coveralls integration - http://coveralls.io/
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

// check the latest version of dependencies
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.8")
