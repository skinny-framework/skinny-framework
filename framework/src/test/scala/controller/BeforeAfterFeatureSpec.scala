package controller

import org.scalatra.test.scalatest._
import skinny._

class BeforeAfterFeatureSpec extends ScalatraFlatSpec {

  behavior of "BeforeAterFeature"

  object First extends SkinnyController with Routes {
    beforeAction() { set("x", "foo") }
    before("/first/*") { set("y", "Y!") }
    beforeAction() { set("z", "zzz") }

    def index = requestScope("x").orNull[String]

    get("/first")(index).as('index)
    get("/first/y")(requestScope("y").orNull[String])
    get("/first/z")(requestScope("z").orNull[String]).as('z)
  }

  object Second extends SkinnyController with Routes {
    beforeAction(only = Seq('bar)) { set("x", "bar") }

    def index = requestScope("x").orNull[String]
    def bar = requestScope("x").orNull[String]

    get("/second")(index).as('index)
    get("/second/bar")(bar).as('bar)
    get("/second/y")(requestScope("y").orNull[String])
  }

  object Third extends SkinnyController with Routes {
    beforeAction(only = Seq('buzz)) { set("x", "BUZZ") }

    def bar = requestScope("x").orNull[String]
    def buzz = requestScope("x").orNull[String]

    get("/third")(bar).as('bar)
    get("/third/buzz")(buzz).as('buzz)
    get("/third/y")(requestScope("y").orNull[String])
  }

  addFilter(First, "/*")
  addFilter(Second, "/*")
  addFilter(Third, "/*")

  "GET /first" should "return 'foo'" in {
    get("/first") {
      body should equal("foo")
    }
  }
  "GET /first/y" should "return 'Y!'" in {
    get("/first/y") {
      body should equal("Y!")
    }
  }
  "GET /first/z" should "return 'zzz'" in {
    get("/first/z") {
      body should equal("zzz")
    }
  }

  "GET /second" should "return empty" in {
    get("/second") {
      body should equal("")
    }
  }
  "GET /second/bar" should "return 'bar'" in {
    get("/second/bar") {
      body should equal("bar")
    }
  }
  "GET /second/y" should "return empty" in {
    get("/second/y") {
      body should equal("")
    }
  }

  "GET /third" should "return empty" in {
    get("/third") {
      body should equal("")
    }
  }
  "GET /third/buzz" should "return 'BUZZ'" in {
    get("/third/buzz") {
      body should equal("BUZZ")
    }
  }
  "GET /third/y" should "return empty" in {
    get("/third/y") {
      body should equal("")
    }
  }

}
