addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.3")

// - scalatra-sbt 0.3.3 uses xsbt-web-plugin 0.5.0
// - xsbt-web-plugin works with sbt 0.13.1 or higher since 0.7.0 see https://github.com/earldouglas/xsbt-web-plugin/issues/142
// - 0.7.0 causes "error: reference to test is ambiguous" with assembly plugin 
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.6.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.2")

