// --------------------------------------------
// sbt plugins for this Skinny app project
// --------------------------------------------
resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
// https://github.com/sbt/sbt/issues/2217
fullResolvers ~= { _.filterNot(_.name == "jcenter") }

// Much fatster dependency resolver - https://github.com/alexarchambault/coursier
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.3")

// --------
// scalac options for sbt
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// --------
// Servlet app packager/runner plugin
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin" % "3.0.4")

// Scalate template files precompilation
addSbtPlugin("org.scalatra.scalate" % "sbt-scalate-precompiler" % "1.9.0.0")

// --------
// format Scala source code automatically
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

// --------
// scoverage for test coverage (./skinny test:coverage)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
// Coveralls integration - http://coveralls.io/
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.5")

// check the latest version of dependencies
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")
