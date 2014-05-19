package skinny.orm.feature

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ Matchers, FunSpec }
import skinny.ParamType

/**
 * Created by lloydmeta on 4/28/14.
 */
class StrongParametersFeatureSpec extends FunSpec with Matchers with StrongParametersFeature {

  describe("#getTypedValueFromStrongParameter") {

    describe("Option[Any] handling") {

      it("should return a value (not wrapped in an option) as a Some(value parsed)") {
        val r = getTypedValueFromStrongParameter("myfield", "hello world", ParamType.String)
        r.get.asInstanceOf[String] should be("hello world")
      }

      it("should return a Some(value) as Some(value parsed)") {
        val r = getTypedValueFromStrongParameter("myfield", Some(3L), ParamType.Long)
        r.get.asInstanceOf[Long] should be(3L)
      }

    }

  }
}
