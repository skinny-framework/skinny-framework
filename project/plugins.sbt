addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.4")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.5")

// scoverage 0.95 doesn't work with Skinny (https://github.com/skinny-framework/skinny-framework/issues/97)
//resolvers += Classpaths.sbtPluginReleases
//addSbtPlugin("com.sksamuel.scoverage" % "sbt-scoverage" % "0.95.9")

// TODO java.lang.ArrayIndexOutOfBoundsException: -1 with 0.13.2-MX
//scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

