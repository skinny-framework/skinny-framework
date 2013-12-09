package skinny.controller

import org.scalatra.test.scalatest._

class JSONFeatureSpec extends ScalatraFlatSpec {

  behavior of "JSONFeature"

  case class Sample(id: Long, firstName: String)

  // SkinnyController has JSONFeature
  object SampleController extends SkinnyController {

    def toJSONString1 = toJSONString(Sample(1, "Alice"))
    def toJSONString2 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))
    def toJSONString3 = toPrettyJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))

    def toJSONString4 = toJSONString(Sample(1, "Alice"), false)
    def toJSONString5 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")), false)
    def toJSONString6 = toPrettyJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")), false)

    def fromJSON1: Option[Sample] = fromJSON[Sample]("""{"id":1,"first_name":"Alice"}""")
    def fromJSON2: Option[List[Sample]] = fromJSON[List[Sample]]("""[{"id":1,"first_name":"Alice"},{"id":2,"first_name":"Bob"}]""")

    def fromJSON3: Option[Sample] = fromJSON[Sample]("""{"id":1,"firstName":"Alice"}""")
    def fromJSON4: Option[List[Sample]] = fromJSON[List[Sample]]("""[{"id":1,"firstName":"Alice"},{"id":2,"firstName":"Bob"}]""")
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
  }

  it should "have fromJSON" in {
    SampleController.fromJSON1.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON2.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))

    SampleController.fromJSON3.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON4.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))
  }

}
