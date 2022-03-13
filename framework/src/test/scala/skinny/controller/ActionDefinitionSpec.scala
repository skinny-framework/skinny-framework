package skinny.controller

import org.scalatest._
import skinny.micro.constant.HttpMethod

class ActionDefinitionSpec extends FlatSpec with Matchers {

  behavior of "ActionDefinition"

  it should "be available" in {
    val method     = HttpMethod.apply("GET")
    val definition = ActionDefinition("index", method, (m: HttpMethod, path: String) => true)

    definition.name should equal("index")
    definition.method should equal(method)
    definition.matcher.apply(null, null) should equal(true)
  }

}
