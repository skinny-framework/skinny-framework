package skinny.controller.feature

import org.scalatest._
import skinny.controller.SkinnyApiController

class EnvFeatureSpec extends FlatSpec with Matchers {

  behavior of "EnvFeature"

  it should "have predicate methods" in {
    class Controller extends SkinnyApiController {
      isDevelopment() should equal(false)
      isTest() should equal(true)
      isStaging() should equal(false)
      isProduction() should equal(false)
    }
  }

  it should "Scalatra default value should be converted to lower case" in {
    class Controller extends SkinnyApiController {
      skinnyEnv should equal(Option("development"))
    }
  }

}
