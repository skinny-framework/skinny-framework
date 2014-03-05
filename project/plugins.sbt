addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.4")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.3")

// scoverage doesn't work with Skinny
//resolvers += Classpaths.sbtPluginReleases
//addSbtPlugin("com.sksamuel.scoverage" % "sbt-scoverage" % "0.95.7")
//addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")


