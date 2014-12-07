package skinny.controller

import org.scalatest._
import skinny.controller.feature.AngularJSSpecification

class AngularJSSpecificationSpec extends FlatSpec with Matchers {

  behavior of "AngularJSSpecification"

  it should "be available" in {
    AngularJSSpecification.version should equal("1.3")
    AngularJSSpecification.xsrfCookieName should equal("XSRF-TOKEN")
    AngularJSSpecification.xsrfHeaderName should equal("X-XSRF-TOKEN")
  }

}
