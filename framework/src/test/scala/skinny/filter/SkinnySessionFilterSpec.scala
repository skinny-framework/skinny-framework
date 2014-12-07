package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.SkinnyController
import skinny.routing.Routes
import skinny.session.{ CreateTables, Connection }

class SkinnySessionFilterSpec extends ScalatraFlatSpec with Connection with CreateTables {
  behavior of "SkinnySessionFilter"

  object Controller extends SkinnyController with SkinnySessionFilter with Routes {
    get("/")("ok").as('index)
  }
  addFilter(Controller, "/*")

  it should "work" in {
    get("/") {
      status should equal(200)
    }
  }

}
