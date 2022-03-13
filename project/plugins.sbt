resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging"
resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
// https://github.com/sbt/sbt/issues/2217
fullResolvers ~= { _.filterNot(_.name == "jcenter") }

addSbtPlugin("org.scalatra.scalate" % "sbt-scalate-precompiler" % "1.9.7.0")
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin"      % "3.0.11")
addSbtPlugin("com.geirsson"         % "sbt-scalafmt"            % "1.5.1")
addSbtPlugin("com.github.sbt"       % "sbt-pgp"                 % "2.1.2")
addSbtPlugin("net.virtual-void"     % "sbt-dependency-graph"    % "0.9.2")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"             % "0.4.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
