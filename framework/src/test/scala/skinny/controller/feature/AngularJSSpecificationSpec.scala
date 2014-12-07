package skinny.controller.feature

import org.scalatest._

class AngularJSSpecificationSpec extends FlatSpec with Matchers {

  behavior of "AngularJSSpecification"

  it should "be available" in {
    AngularJSSpecification.version should equal("1.3")
    AngularJSSpecification.xsrfCookieName should equal("XSRF-TOKEN")
    AngularJSSpecification.xsrfHeaderName should equal("X-XSRF-TOKEN")
  }

}
