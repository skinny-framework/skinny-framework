resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"
// https://github.com/sbt/sbt/issues/2217
fullResolvers ~= { _.filterNot(_.name == "jcenter") }

addSbtPlugin("org.skinny-framework" % "sbt-scalate-precompiler" % "1.7.1.0")
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin"      % "2.0.4")
addSbtPlugin("org.scalariform"      % "sbt-scalariform"         % "1.6.0")
addSbtPlugin("com.github.mpeltonen" % "sbt-idea"                % "1.6.0")
addSbtPlugin("com.jsuereth"         % "sbt-pgp"                 % "1.0.0")
addSbtPlugin("net.virtual-void"     % "sbt-dependency-graph"    % "0.8.2")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"             % "0.1.10")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
