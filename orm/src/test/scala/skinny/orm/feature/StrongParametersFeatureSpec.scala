package skinny.orm.feature

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import skinny.ParamType

class StrongParametersFeatureSpec extends AnyFunSpec with Matchers with StrongParametersFeature {

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

    describe("empty string handling") {

      it("should return null as BigDecimal") {
        val r = getTypedValueFromStrongParameter("myfield", "", ParamType.BigDecimal)
        r.get.asInstanceOf[BigDecimal] should be(null)
      }

      it("should return null as String") {
        val r = getTypedValueFromStrongParameter("myfield", "", ParamType.String)
        r.get.asInstanceOf[String] should be("")
      }

      it("should return null as DateTime") {
        import org.joda.time.{ DateTime => JDateTime }
        val r = getTypedValueFromStrongParameter("myfield", "", ParamType.DateTime)
        r.get.asInstanceOf[JDateTime] should be(null)
      }
    }

    describe("blank string handling") {

      it("should return null as BigDecimal") {
        val r = getTypedValueFromStrongParameter("myfield", "  ", ParamType.BigDecimal)
        r.get.asInstanceOf[BigDecimal] should be(null)
      }

      it("should return null as String") {
        val r = getTypedValueFromStrongParameter("myfield", "  ", ParamType.String)
        r.get.asInstanceOf[String] should be("  ")
      }

      it("should return null as DateTime") {
        import org.joda.time.{ DateTime => JDateTime }
        val r = getTypedValueFromStrongParameter("myfield", "  ", ParamType.DateTime)
        r.get.asInstanceOf[JDateTime] should be(null)
      }
    }
  }
}
