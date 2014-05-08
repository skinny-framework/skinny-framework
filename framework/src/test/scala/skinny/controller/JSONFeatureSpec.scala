package skinny.controller

import org.scalatra.test.scalatest._
import skinny.Routes

// on Scala 2.10.0 ScalaTest #equal matcher with inner case classes works but it fails on higher version
case class Sample(id: Long, firstName: String)
case class Person(name: Option[String] = None, parent: Person, children: Seq[Person] = Nil)

class JSONFeatureSpec extends ScalatraFlatSpec {

  behavior of "JSONFeature"

  // SkinnyController has JSONFeature
  object SampleController extends SkinnyController with Routes {

    get("/responseAsJson") {
      responseAsJSON(Sample(1, "Alice"))
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

    def fromJSON1: Option[Sample] = fromJSONString[Sample]("""{"id":1,"first_name":"Alice"}""")
    def fromJSON2: Option[List[Sample]] = fromJSONString[List[Sample]]("""[{"id":1,"first_name":"Alice"},{"id":2,"first_name":"Bob"}]""")

    def fromJSON3: Option[Sample] = fromJSONString[Sample]("""{"id":1,"firstName":"Alice"}""")
    def fromJSON4: Option[List[Sample]] = fromJSONString[List[Sample]]("""[{"id":1,"firstName":"Alice"},{"id":2,"firstName":"Bob"}]""")

    def fromJSON5: Option[Person] = fromJSONString[Person](
      """{"name":"Dennis","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]},{"name":"Chris","parent":{"name":"Alice","parent":null,"children":[]},"children":[{"name":"Bob","parent":{"name":"Alice","parent":null,"children":[]},"children":[]}]}]}""")
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

    addFilter(SampleController, "/*")
    get("/responseAsJson") {
      body should equal("""{"id":1,"first_name":"Alice"}""")
    }
  }

  it should "have fromJSON" in {
    SampleController.fromJSON1.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON2.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))

    SampleController.fromJSON3.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON4.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))
    SampleController.fromJSON5.get should equal(SampleController.dennis)
  }

}
