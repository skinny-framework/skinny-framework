package skinny.validator

import org.scalatest._
import skinny.validator.implicits.ParametersGetAsImplicits

class ParametersSpec extends FlatSpec with Matchers with ParametersGetAsImplicits {

  behavior of "Params"

  it should "be available" in {
    val validations: Validations = Validations(Map(), Nil)
    val instance = ParametersFromValidations(validations)
    instance should not be null
  }

  it should "return value with values" in {
    val map: Map[String, Any] = Map(
      "name" -> "Alice",
      "age" -> 19,
      "active" -> true,
      "average_point" -> 0.12D
    )
    val params = ParametersFromMap(map)
    params.get("name") should equal(Some("Alice"))
    params.getAs[Int]("age") should equal(Some(19))
    params.getAs[Short]("age") should equal(Some(19))
    params.getAs[Byte]("age") should equal(Some(19))
    params.getAs[Boolean]("active") should equal(Some(true))
    params.getAs[Double]("average_point") should equal(Some(0.12D))
    params.getAs[Float]("average_point") should equal(Some(0.12F))
  }

}
