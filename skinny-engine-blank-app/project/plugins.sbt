resolvers += Classpaths.sbtPluginReleases
addMavenResolverPlugin

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin"      % "2.0.0")
addSbtPlugin("org.skinny-framework" % "sbt-scalate-precompiler" % "1.7.1.0")
addSbtPlugin("com.typesafe.sbt"     % "sbt-scalariform"         % "1.3.0")
addSbtPlugin("com.typesafe.sbt"     % "sbt-native-packager"     % "1.0.3")
