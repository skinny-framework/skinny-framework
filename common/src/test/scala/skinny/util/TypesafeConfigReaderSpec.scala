package skinny.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TypesafeConfigReaderSpec extends AnyFunSpec with Matchers {

  describe("TypesafeConfigReader#load") {

    it("should read application.conf") {
      val config = TypesafeConfigReader.load("application.conf")
      config.getString("name") should equal("bar")
      config.getLong("num") should equal(123)
      intercept[com.typesafe.config.ConfigException] {
        config.getLong("dummy")
      }
    }

    it("should read messages.conf") {
      val config = TypesafeConfigReader.load("messages.conf")
      config.getString("name") should equal("Name")
      intercept[com.typesafe.config.ConfigException] {
        config.getLong("foo.name")
      }
    }
  }

  describe("TypesafeConfigReader#loadAsMap") {

    it("should read application.conf") {
      val config = TypesafeConfigReader.loadAsMap("application.conf")
      config.get("name") should equal(Some("bar"))
      config.get("num") should equal(Some("123"))
      config.get("dummy") should equal(None)
    }

    it("should read messages.conf") {
      val config = TypesafeConfigReader.loadAsMap("messages.conf")
      config.get("name") should equal(Some("Name"))
      config.get("foo.name") should equal(Some("FooName"))
    }
  }

}
