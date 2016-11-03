// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------
resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
// https://github.com/sbt/sbt/issues/2217
fullResolvers ~= { _.filterNot(_.name == "jcenter") }

// Much fatster dependency resolver - https://github.com/alexarchambault/coursier
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M14")

// --------
// scalac options for sbt
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// --------
// Servlet app packager/runner plugin
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin" % "2.0.6")

// Scalate template files precompilation
addSbtPlugin("org.skinny-framework" % "sbt-scalate-precompiler" % "1.8.0.0-RC1")

// --------
// format Scala source code automatically
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// --------
// IntelliJ IDEA setting files generator
// If you don't need this, remove org.sbtidea.SbtIdeaPlugin._ and ideaExcludeFolders too
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// --------
// scoverage for test coverage (./skinny test:coverage)
// addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")
// Coveralls integration - http://coveralls.io/
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

// check the latest version of dependencies
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")
