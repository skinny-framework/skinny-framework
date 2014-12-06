package skinny.controller

import org.scalatra.test.scalatest._
import skinny._

class BeforeAfterFeatureSpec extends ScalatraFlatSpec {

  behavior of "BeforeAterFeature"

  object First extends SkinnyController with Routes {
    beforeAction() { set("x", "foo") }
    before() { set("y", "Y!") } // Scalatra's filter
    beforeAction() { set("z", "zzz") }

    def index = getFromRequestScope("x").orNull[String]

    get("/first")(index).as('index)
    get("/first/y")(getFromRequestScope("y").orNull[String])
    get("/first/z")(getFromRequestScope("z").orNull[String]).as('z)
  }

  object Second extends SkinnyController with Routes {
    beforeAction(only = Seq('filtered)) { set("x", "bar") }

    def index = getFromRequestScope("x").orNull[String]
    def updatedByBeforeAction = getFromRequestScope("x").orNull[String]

    get("/second")(index).as('index)
    get("/second/y")(getFromRequestScope("y").orNull[String])
    get("/second/filtered")(updatedByBeforeAction).as('filtered)
  }

  object Third extends SkinnyController with Routes {

    def bar = getFromRequestScope("x").orNull[String]
    def buzz = getFromRequestScope("x").orNull[String]

    get("/third")(bar).as('bar)
    get("/third/y")(getFromRequestScope("y").orNull[String])
  }

  addFilter(First, "/*")
  addFilter(Second, "/*") // filters in First shouldn't effect this controller
  addFilter(Third, "/*")

  "beforeAction" should "work" in {
    get("/first") {
      body should equal("foo")
    }
    get("/first/y") {
      body should equal("Y!")
    }
    get("/first/z") {
      body should equal("zzz")
    }
  }

  "beforeAction with only" should "work" in {
    // beforeAction in First should not effect this controller
    get("/second") {
      body should equal("")
    }
    get("/second/filtered") {
      body should equal("bar")
    }
    get("/second/y") {
      body should equal("Y!")
    }
  }

  "beforeAction" should "be controller-local" in {
    // beforeAction in First should not effect this controller
    get("/third") {
      body should equal("")
    }
    get("/third/y") {
      body should equal("Y!")
    }
  }
}

class BeforeActionSpec extends ScalatraFlatSpec {
  behavior of "beforeAction"

  object Before1 extends SkinnyController with Routes {
    get("/1") { response.writer.write("2") }.as('index)
    beforeAction() { response.writer.write("0") }
    beforeAction() { response.writer.write("1") }
  }
  object Before2 extends SkinnyController with Routes {
    get("/2") { response.writer.write("Computer") }.as('index)
    beforeAction() { response.writer.write("OK ") }
  }
  addFilter(Before1, "/*")
  addFilter(Before2, "/*")

  "beforeAction" should "be controller-local" in {
    get("/1") {
      body should equal("012")
    }
    get("/2") {
      body should equal("OK Computer")
    }
  }

}

class AfterActionSpec extends ScalatraFlatSpec {
  behavior of "afterAction"

  object After1 extends SkinnyController with Routes {
    get("/1") { response.writer.write("0") }.as('index)
    afterAction() { response.writer.write("1") }
    afterAction() { response.writer.write("2") }
  }
  object After2 extends SkinnyController with Routes {
    get("/2") { response.writer.write("OK") }.as('index)
    afterAction() { response.writer.write(" Computer") }
  }
  addFilter(After1, "/*")
  addFilter(After2, "/*")

  "afterAction" should "be controller-local" in {
    get("/1") {
      body should equal("012")
    }
    get("/2") {
      body should equal("OK Computer")
    }
  }

}
