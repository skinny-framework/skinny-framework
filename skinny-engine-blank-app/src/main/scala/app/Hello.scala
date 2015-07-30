package app

import skinny.engine._

trait MessageExtractor { self: SkinnyEngineBase =>
  def message(implicit ctx: Context): String = {
    val name = params.getOrElse("name", "Anonymous")
    s"Hello, $name"
  }
}

object Hello extends WebApp with MessageExtractor {

  get("/") {
    redirect("/hello")
  }

  get("/hello")(message)

  post("/hello")(message)

  get("/hello/json") {
    responseAsJSON(Map("message" -> message))
  }
}

import skinny.engine.scalate._
import skinny.engine.async._
import scala.concurrent._

object AsyncHello extends AsyncWebApp with MessageExtractor with ScalateSupport {

  get("/hello/scalate") { implicit ctx =>
    contentType = "text/html"
    ssp("/index", "name" -> "foo")
  }

  get("/hello/async") { implicit ctx =>
    Future {
      message
    }
  }

  get("/hello/json/async") { implicit ctx =>
    contentType = "application/json; charset=utf-8"
    Future {
      responseAsJSON(Map("message" -> message))
    }
  }
}
