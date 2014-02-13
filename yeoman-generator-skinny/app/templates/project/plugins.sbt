addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.3")

// - scalatra-sbt 0.3.3 uses xsbt-web-plugin 0.5.0
// - xsbt-web-plugin works with sbt 0.13.1 or higher since 0.7.0 see https://github.com/earldouglas/xsbt-web-plugin/issues/142
// - 0.7.0 causes "error: reference to test is ambiguous" with assembly plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.6.0")

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

// scalarifrom (disabled by default for beginners)
//addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.1")

// for IntelliJ IDEA users
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

// for Scala IDE users
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

// for ./skinny package:standalone
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.2")

