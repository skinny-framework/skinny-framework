resolvers += Classpaths.sbtPluginReleases
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")
