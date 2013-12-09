package skinny.controller

import org.scalatra.test.scalatest._

class JSONFeatureSpec extends ScalatraFlatSpec {

  behavior of "JSONFeature"

  case class Sample(id: Long, name: String)

  // SkinnyController has JSONFeature
  object SampleController extends SkinnyController {

    def toJSONString1 = toJSONString(Sample(1, "Alice"))
    def toJSONString2 = toJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))
    def toJSONString3 = toPrettyJSONString(List(Sample(1, "Alice"), Sample(2, "Bob")))

    def fromJSON1: Option[Sample] = fromJSON[Sample]("""{"id":1,"name":"Alice"}""")
    def fromJSON2: Option[List[Sample]] = fromJSON[List[Sample]]("""[{"id":1,"name":"Alice"},{"id":2,"name":"Bob"}]""")
  }

  it should "have toJSON" in {
    SampleController.toJSONString1 should equal("""{"id":1,"name":"Alice"}""")
    SampleController.toJSONString2 should equal("""[{"id":1,"name":"Alice"},{"id":2,"name":"Bob"}]""")
    SampleController.toJSONString3 should equal(
      """[ {
        |  "id" : 1,
        |  "name" : "Alice"
        |}, {
        |  "id" : 2,
        |  "name" : "Bob"
        |} ]""".stripMargin)
  }

  it should "have fromJSON" in {
    SampleController.fromJSON1.get should equal(Sample(1, "Alice"))
    SampleController.fromJSON2.get should equal(List(Sample(1, "Alice"), Sample(2, "Bob")))
  }

}
