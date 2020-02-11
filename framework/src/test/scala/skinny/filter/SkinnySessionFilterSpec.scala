package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.SkinnyController
import skinny.controller.AsyncSkinnyController
import skinny.routing.Routes
import skinny.session.{ Connection, CreateTables }

class SkinnySessionFilterSpec extends ScalatraFlatSpec with Connection with CreateTables {
  behavior of "SkinnySessionFilter"

  object SyncController extends SkinnyController with SkinnySessionFilter with Routes {
    get("/sync")("ok").as("index")
  }
  addFilter(SyncController, "/*")

  object AsyncController extends AsyncSkinnyController with AsyncSkinnySessionFilter with Routes {
    get("/async") { implicit ctx =>
      "ok"
    }.as("index")
  }
  addFilter(AsyncController, "/*")

  it should "work with sync version" in {
    get("/sync") {
      status should equal(200)
    }
  }
  it should "work with async version" in {
    get("/sync") {
      status should equal(200)
    }
  }

}
