import skinny.engine._
import scala.concurrent._

object Application extends App {

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

}
