#!/usr/bin/env scalas
// or ./scalas app.scala
/***
scalaVersion := "2.11.7"
resolvers += "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
libraryDependencies += "org.skinny-framework" %% "skinny-engine-server" % "2.0.0.M3"
*/
import skinny.engine._
import scala.concurrent._

println
println(" Access localhost:8081 ")
println

WebServer.mount(new WebApp {
  get("/") {
    val name = params.getOrElse("name", "Anonymous")
    s"Hello, $name"
  }
}).mount(new AsyncWebApp {
  get("/async") { implicit ctx =>
    Future {
      responseAsJSON(params)
    }
  }
}).port(8081).run()

