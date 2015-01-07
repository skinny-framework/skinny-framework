package skinny.util

import org.scalatest._

class StringUtilSpec extends FlatSpec with Matchers {
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
    toCamelCase("FirstName") should equal("firstName")
    toCamelCase("firstName") should equal("firstName")
    toCamelCase("first_name") should equal("firstName")
    toCamelCase("_first_name") should equal("firstName")
    toCamelCase("is_atm_working") should equal("isAtmWorking")
  }

  it should "have #toUpperCamelCase" in {
    toUpperCamelCase(null) should be(null)
    toUpperCamelCase("FirstName") should equal("FirstName")
    toUpperCamelCase("firstName") should equal("FirstName")
    toUpperCamelCase("first_name") should equal("FirstName")
    toUpperCamelCase("_first_name") should equal("FirstName")
    toUpperCamelCase("is_atm_working") should equal("IsAtmWorking")
  }
}
