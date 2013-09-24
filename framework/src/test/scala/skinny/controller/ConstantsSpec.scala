package skinny.controller

import org.scalatest._
import org.scalatest.matchers._

class ConstantsSpec extends FlatSpec with ShouldMatchers {

  behavior of "Constants"

  it should "be available" in {
    Constants.RouteMetadataHttpMethodCacheKey should equal('HttpMethodSavedBySkinnyFramework)
  }

}
