package skinny.activeimplicits

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InflectorImplicitsSpec extends AnyFlatSpec with Matchers with InflectorImplicits {

  it should "have implicits" in {
    "company".pluralize should equal("companies")
    "companies".singularize should equal("company")

    "coin".pluralize should equal("coins")
    "coins".singularize should equal("coin")

    "person".pluralize should equal("people")
    "people".singularize should equal("person")
  }

}
