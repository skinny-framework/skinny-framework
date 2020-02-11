package integrationtest

import controller.{ AngularXHRProgrammersController, Controllers }
import model._
import skinny.json.AngularJSONStringOps._
import skinny.test._

class AngularXHRProgrammersControllerSpec extends SkinnyFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.angularApp, "/*")
  addFilter(AngularXHRProgrammersController, "/*")

  it should "return Angular app html" in {
    get("/angular/app") {
      logger.debug(body)
      status should equal(200)
      response.headers("Set-Cookie").exists(_.matches("XSRF-TOKEN=.+")) should equal(true)
    }
  }

  // Angular XHR APIs

  it should "accept resource list request" in {
    val programmer = Programmer.findAllWithLimitOffset(1, 0).headOption.getOrElse {
      FactoryGirl(Programmer).create("name" -> "Alice")
    }
    get("/angular/programmers.json") {
      logger.debug(body)
      status should equal(200)
      val acts = fromJSONString[List[Programmer]](body)
      acts should not equal (None)
      acts.get should not be (empty)
      atLeast(1, acts.get) should have(Symbol("name")(programmer.name))
    }
  }

  it should "reject XSRF requests" in {
    post("/angular/programmers.json") {
      status should equal(403)
    }
    put("/angular/programmers/123.json") {
      status should equal(403)
    }
    patch("/angular/programmers/123.json") {
      status should equal(403)
    }
    delete("/angular/programmers/123.json") {
      status should equal(403)
    }
  }

  it should "provide valid XSRF protection" in {
    session {
      // retrive XSRF-TOKEN value
      var token = ""
      get("/angular/app") {
        status should equal(200)
        response.headers("Set-Cookie").find(_.matches("XSRF-TOKEN=.+")).foreach { setCookie =>
          token = setCookie.drop(11).split(";")(0)
        }
        token should not equal ("")
      }

      // access with X-XSRF-TOKEN header
      val json = """{"name":"Alice","favoriteNumber":"777","plainTextPassword":"plain-text-password"}"""
      post("/angular/programmers.json",
           json,
           Map("X-XSRF-TOKEN" -> token, "Content-Type" -> "application/json;charset=utf-8")) {
        status should equal(201)
      }

      // TODO 500
      //      post("/angular/programmers/abc.json", json, Map("X-XSRF-TOKEN" -> token, "Content-Type" -> "application/json;charset=utf-8")) {
      //        status should equal(400)
      //      }
    }
  }

}
