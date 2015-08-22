resolvers += Classpaths.sbtPluginReleases
resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases"

addMavenResolverPlugin

addSbtPlugin("org.skinny-framework" % "sbt-scalate-precompiler" % "1.7.1.0")
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin"      % "2.0.1")
addSbtPlugin("com.typesafe.sbt"     % "sbt-scalariform"         % "1.3.0")
addSbtPlugin("com.github.mpeltonen" % "sbt-idea"                % "1.6.0")
addSbtPlugin("com.jsuereth"         % "sbt-pgp"                 % "1.0.0")
addSbtPlugin("net.virtual-void"     % "sbt-dependency-graph"    % "0.7.5")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"             % "0.1.9")
// 1.2.0 looks unstable
addSbtPlugin("org.scoverage"        % "sbt-scoverage"           % "1.1.0")
addSbtPlugin("org.scoverage"        % "sbt-coveralls"           % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

