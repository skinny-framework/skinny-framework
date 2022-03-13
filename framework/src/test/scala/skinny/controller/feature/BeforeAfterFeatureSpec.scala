package skinny.controller.feature

import org.scalatra.test.scalatest._
import skinny._

class BeforeAfterFeatureSpec extends ScalatraFlatSpec {

  behavior of "BeforeAterFeature"

  object First extends SkinnyController with Routes {
    beforeAction() { set("x", "foo") }
    before() { set("y", "Y!") } // Scalatra's filter
    beforeAction() { set("z", "zzz") }

    def index = getFromRequestScope("x").orNull[String]

    get("/first")(index).as("index")
    get("/first/y")(getFromRequestScope("y").orNull[String])
    get("/first/z")(getFromRequestScope("z").orNull[String]).as("z")
  }

  object Second extends SkinnyController with Routes {
    beforeAction(only = Seq("filtered")) { set("x", "bar") }

    def index                 = getFromRequestScope("x").orNull[String]
    def updatedByBeforeAction = getFromRequestScope("x").orNull[String]

    get("/second")(index).as("index")
    get("/second/y")(getFromRequestScope("y").orNull[String])
    get("/second/filtered")(updatedByBeforeAction).as("filtered")
  }

  object Third extends SkinnyController with Routes {

    def bar  = getFromRequestScope("x").orNull[String]
    def buzz = getFromRequestScope("x").orNull[String]

    get("/third")(bar).as("bar")
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
      // body should equal("Y!")
      // skinny-micro's filter won't be applied here
      body should equal("")
    }
  }

  "beforeAction" should "be controller-local" in {
    // beforeAction in First should not effect this controller
    get("/third") {
      body should equal("")
    }
    get("/third/y") {
      // body should equal("Y!")
      // skinny-micro's filter won't be applied here
      body should equal("")
    }
  }
}
