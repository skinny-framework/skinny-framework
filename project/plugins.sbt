resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
// https://github.com/sbt/sbt/issues/2217
fullResolvers ~= { _.filterNot(_.name == "jcenter") }

addSbtPlugin("io.get-coursier"      % "sbt-coursier"            % "1.0.3")
addSbtPlugin("org.scalatra.scalate" % "sbt-scalate-precompiler" % "1.9.0.0")
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin"      % "3.0.0")
addSbtPlugin("com.lucidchart"       % "sbt-scalafmt"            % "1.15")
addSbtPlugin("com.jsuereth"         % "sbt-pgp"                 % "1.1.1")
addSbtPlugin("net.virtual-void"     % "sbt-dependency-graph"    % "0.9.0")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"             % "0.3.4")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
