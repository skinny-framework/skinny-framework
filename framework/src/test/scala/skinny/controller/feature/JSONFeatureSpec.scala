package skinny.controller.feature

import org.scalatra.test.scalatest._
import skinny.controller.{ SkinnyController, SkinnyServlet }
import skinny.micro.async.AsyncResult

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.Try

// on Scala 2.10.0 ScalaTest #equal matcher with inner case classes works but it fails on higher version
case class Sample(id: Long, firstName: String)
case class Person(name: Option[String] = None, parent: Person, children: Seq[Person] = Nil)

class JSONFeatureSpec extends ScalatraFlatSpec {

  behavior of "JSONFeature"

  object SampleController extends SkinnyServlet {

    get("/sync") {
      responseAsJSON(Sample(1, "Alice"))
    }

    get("/async") {
      val fSample = Future { Sample(1, "Alice") }
      new AsyncResult() {
        override val is: Future[_] = fSample.map(responseAsJSON(_))
      }
    }

    def toJSONString1 = toJSONString(Sample(1, "Alice"))
    def toJSONString2 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))
    def toJSONString3 = toPrettyJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))

    def toJSONString4 = toJSONString(Sample(1, "Alice"), false)
    def toJSONString5 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")), false)
    def toJSONString6 = toPrettyJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")), false)

    val alice = Person(Some("Alice"), null)
    val bob = Person(Some("Bob"), alice, Nil)
    val chris = Person(Some("Chris"), alice, Seq(bob))
    val dennis = Person(Some("Dennis"), alice, Seq(bob, chris))

    def toJSONString7 = toJSONString(dennis)

    def fromJSON1: Try[Sample] = fromJSONString[Sample]("""{"id":1,"first_name":"Alice"}""")
    def fromJSON2: Try[List[Sample]] = fromJSONString[List[Sample]]("""[{"id":1,"first_name":"Alice"},{"id":2,"first_name":"Bob"}]""")

    def fromJSON3: Try[Sample] = fromJSONString[Sample]("""{"id":1,"firstName":"Alice"}""")
    def fromJSON4: Try[List[Sample]] = fromJSONString[List[Sample]]("""[{"id":1,"firstName":"Alice"},{"id":2,"firstName":"Bob"}]""")

    def fromJSON5: Try[Person] = fromJSONString[Person](
      """{"name":"Dennis","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]},{"name":"Chris","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]}]}]}""")
  }

  object Sample2Controller extends SkinnyController {

    override def useUnderscoreKeysForJSON = false
    override def useJSONVulnerabilityProtection = true

    def toJSONString1 = toJSONString(Sample(1, "Alice"))
    def toJSONString2 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))
  }

  it should "have toJSON" in {
    SampleController.toJSONString1 should equal("""{"id":1,"first_name":"Alice"}""")
    SampleController.toJSONString2 should equal("""[{"id":1,"first_name":"Alice"},{"id":2,"first_name":"Bob"}]""")
    SampleController.toJSONString3 should equal(
      """[ {
        |  "id" : 1,
        |  "first_name" : "Alice"
        |}, {
        |  "id" : 2,
        |  "first_name" : "Bob"
        |} ]""".stripMargin)

    SampleController.toJSONString4 should equal("""{"id":1,"firstName":"Alice"}""")
    SampleController.toJSONString5 should equal("""[{"id":1,"firstName":"Alice"},{"id":2,"firstName":"Bob"}]""")
    SampleController.toJSONString6 should equal(
      """[ {
        |  "id" : 1,
        |  "firstName" : "Alice"
        |}, {
        |  "id" : 2,
        |  "firstName" : "Bob"
        |} ]""".stripMargin)

    SampleController.toJSONString7 should equal(
      """{"name":"Dennis","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]},{"name":"Chris","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]}]}]}""")

    // Normal synced responseAsJson should work
    addServlet(SampleController, "/*")
    get("/sync") {
      body should equal("""{"id":1,"first_name":"Alice"}""")
    }

    // Test the async version
    import scala.concurrent.ExecutionContext.Implicits.global
    val listOfFutureBodies = (1 to 3).map(_ => Future { get("/async") { body } })
    val fListOfBodies = Future.sequence(listOfFutureBodies)
    Await.result(fListOfBodies, 5.seconds).foreach(_ should equal("""{"id":1,"first_name":"Alice"}"""))
  }

  it should "have fromJSON" in {
    SampleController.fromJSON1.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON2.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))

    SampleController.fromJSON3.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON4.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))
    SampleController.fromJSON5.get should equal(SampleController.dennis)
  }

  it should "have toJSON for camelCase" in {
    Sample2Controller.toJSONString1 should equal(
      """)]}',
        |{"id":1,"firstName":"Alice"}""".stripMargin)
    Sample2Controller.toJSONString2 should equal(
      """)]}',
        |[{"id":1,"firstName":"Alice"},{"id":2,"firstName":"Bob"}]""".stripMargin)
  }

}
