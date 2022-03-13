package skinny.controller.feature

import skinny._
import org.scalatra.test.scalatest.ScalatraFlatSpec

case class Name(first: String, last: String)

class RequestScopeFeatureSpec extends ScalatraFlatSpec {
  behavior of "RequestScopeFeature"

  object Controller extends SkinnyController with Routes {
    def echo = {
      set("name"          -> params.getAs[String]("name").getOrElse("Anonymous"))
      requestScope("name" -> params.getAs[String]("name").getOrElse("Anonymous"))
      RequestScopeFeature.getAs[String](request, "name").getOrElse("Anonymous")
    }
    def nameModel = {
      setAsParams(Name("Kaz", "Sera"))
      toJSONString(params)
    }
    def showErrorMessages = errorMessages

    get("/echo")(echo).as("echo")
    get("/nameModel")(nameModel).as("nameModel")
    get("/showErrorMessages")(showErrorMessages).as("showErrorMessages")
  }
  addFilter(Controller, "/*")

  it should "have params#getAs[A](key)" in {
    get("/echo") {
      status should equal(200)
      body should equal("Anonymous")
    }
    get("/echo?name=Alice") {
      status should equal(200)
      body should equal("Alice")
    }
  }

  it should "have #setAsParams" in {
    get("/nameModel") {
      status should equal(200)
    }
  }

  it should "have #errorMessages" in {
    get("/showErrorMessages") {
      status should equal(200)
    }
  }

}
