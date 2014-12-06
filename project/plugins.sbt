resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.5.0")
addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed"),
  ExclusionRule(organization = "com.earldouglas")
))
// TODO: scalatra-sbt 0.3.5 is incompatible with xsbt-web-plugin 1.0.0
// https://github.com/scalatra/scalatra-sbt/issues/9
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty"),
  ExclusionRule(organization = "org.eclipse.jetty"),
  ExclusionRule(organization = "org.apache.tomcat.embed")
))

addSbtPlugin("com.typesafe.sbt"     % "sbt-scalariform"      % "1.3.0")
addSbtPlugin("com.github.mpeltonen" % "sbt-idea"             % "1.6.0")
addSbtPlugin("com.jsuereth"         % "sbt-pgp"              % "1.0.0")
addSbtPlugin("net.virtual-void"     % "sbt-dependency-graph" % "0.7.4")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"          % "0.1.7")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"        % "1.0.1")
addSbtPlugin("org.scoverage"        % "sbt-coveralls"        % "1.0.0.BETA1")
addSbtPlugin("org.xerial.sbt"       % "sbt-sonatype"         % "0.2.1")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

