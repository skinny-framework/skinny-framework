package skinny.util

import org.scalatest._

class AngularJSONStringOpsSpec extends FlatSpec with Matchers {

  behavior of "AngularJSONStringOps"

  case class Sample(firstName: String, age: Int)

  it should "be available" in {
    new AngularJSONStringOps {
      useJSONVulnerabilityProtection should equal(true)
      useUnderscoreKeysForJSON should equal(false)
    }
  }

}
