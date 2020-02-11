package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController

class FormParamsFeatureSpec extends ScalatraFlatSpec {
  behavior of "FormParamsFeature"

  class Controller extends SkinnyController {
    def single = formParams.getAs[String]("foo").getOrElse("<empty>")
    def multi  = formMultiParams.getAs[String]("foo").map(_.mkString(",")).getOrElse("<empty>")
  }
  object ctrl extends Controller with Routes {
    get("/get")(single).as("get")
    post("/post")(single).as("post")
    get("/multi/get")(multi).as("multiGet")
    post("/multi/post")(multi).as("multiPost")
  }
  addFilter(ctrl, "/*")

  "formMultiParams" should "be available" in {
    get("/multi/get?foo=bar&foo=baz") {
      status should equal(200)
      body should equal("")
    }
    post("/multi/post", "foo" -> "bar", "foo" -> "baz") {
      status should equal(200)
      body should equal("bar,baz")
    }
    post("/multi/post?foo=bar&foo=baz&foo=xxx", "foo" -> "xxx", "foo" -> "yyy") {
      status should equal(200)
      body should equal("xxx,yyy")
    }
  }

  "formParams" should "be available" in {
    get("/get", "foo" -> "bar") {
      status should equal(200)
      body should equal("<empty>")
    }
    post("/post", "foo" -> "bar") {
      status should equal(200)
      body should equal("bar")
    }
    post("/post?foo=bar", "foo" -> "baz") {
      status should equal(200)
      body should equal("baz")
    }
  }

}
