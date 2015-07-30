lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.11.7",
    resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    libraryDependencies += "org.skinny-framework" %% "skinny-engine-server" % skinnyVersion
  )

val skinnyVersion = "2.0.0.M3"
val jettyVersion = "9.2.12.v20150709"
