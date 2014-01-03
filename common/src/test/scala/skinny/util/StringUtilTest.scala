package skinny.util

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.ParamType

class StringUtilTest extends FlatSpec with ShouldMatchers {
  import StringUtil._

  behavior of "StringUtil"

  it should "have #toSnakeCase" in {
    toSnakeCase(null) should be(null)
    toSnakeCase("firstName") should equal("first_name")
    toSnakeCase("first_name") should equal("first_name")
    toSnakeCase("_first_name") should equal("_first_name")
    toSnakeCase("isATMWorking") should equal("is_atm_working")
  }

  it should "have #toCamelCase" in {
    toCamelCase(null) should be(null)
    toCamelCase("firstName") should equal("firstName")
    toCamelCase("first_name") should equal("firstName")
    toCamelCase("_first_name") should equal("firstName")
    toCamelCase("is_atm_working") should equal("isAtmWorking")
  }
}
