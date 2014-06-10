package skinny.controller

import org.scalatest._

class ConstantsSpec extends FlatSpec with Matchers {

  behavior of "Constants"

  it should "be available" in {
    Constants.RouteMetadataHttpMethodCacheKey should equal('HttpMethodSavedBySkinnyFramework)
  }

}
