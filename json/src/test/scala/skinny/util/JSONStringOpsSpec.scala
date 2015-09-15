package skinny.util

import org.scalatest._
import skinny.json.JSONStringOps

import scala.util.{ Success, Try }

// http://www.playframework.com/documentation/2.2.x/ScalaJson
case class UserResponse(user: User)
case class User(name: String, age: Int, email: String, isAlive: Boolean = false, friend: Option[User] = None)

class JSONStringOpsSpec extends FunSpec with Matchers {

  describe("JSONStringOps#fromJSONString") {

    it("parses Play2 documentation example") {
      val jsonString =
        """{
          |  "user": {
          |    "name" : "toto",
          |    "age" : 25,
          |    "email" : "toto@jmail.com",
          |    "isAlive" : true,
          |    "friend" : {
          |      "name" : "tata",
          |      "age" : 20,
          |      "email" : "tata@coldmail.com"
          |    }
          |  }
          |}
        """.stripMargin

      val userResponse = JSONStringOps.fromJSONString[UserResponse](jsonString)
      userResponse.isSuccess should be(true)

      val user = userResponse.get.user
      user.age should equal(25)
      user.email should equal("toto@jmail.com")
      user.friend.get should equal(User("tata", 20, "tata@coldmail.com"))
      user.isAlive should equal(true)
      user.name should equal("toto")
    }
  }

  describe("JSONStringOps#toJSONString") {

    it("converts Scala objects to JSON string value") {
      val value = Map(
        "name" -> Seq("name is required", "name's length must be less than 32."),
        "somethingLikeThat" -> Nil)
      val result = JSONStringOps.toJSONString(value, true)
      result should equal(
        """{"name":["name is required","name's length must be less than 32."],"something_like_that":[]}""")
    }

  }

  case class Something(fooBarBaz: String, hogeFooBar: Int)
  case class Something2(fooBar_Baz: String, hogeFoo_bar: Int)

  describe("JSONStringOps#fromJSONString for objects") {

    it("converts JSON string value to Something object") {
      val source = Something("abC", 123)
      val json = JSONStringOps.toJSONStringAsIs(source)
      val result: Try[Something] = JSONStringOps.fromJSONString[Something](json, false)
      result.get.fooBarBaz should equal(source.fooBarBaz)
      result.get.hogeFooBar should equal(source.hogeFooBar)
    }

    it("converts snake_cased JSON string value to Something object") {
      val source = Something("abC", 123)
      val json = JSONStringOps.toJSONString(source, true)
      val result: Try[Something] = JSONStringOps.fromJSONString[Something](json, false)
      result.get.fooBarBaz should equal(source.fooBarBaz)
      result.get.hogeFooBar should equal(source.hogeFooBar)
    }

  }

  /* TODO json4s doesn't support fields such as "fooBar_baz"
  describe("JSONStringOps#fromJSONStringAsIs for objects") {

    it("converts JSON string value to Something2 object") {
      val source = Something2("abC", 123)
      val json = JSONStringOps.toJSONStringAsIs(source)
      val result: Option[Something2] = JSONStringOps.fromJSONStringAsIs[Something2](json)
      result.get.fooBar_Baz should equal(source.fooBar_Baz)
      result.get.hogeFoo_bar should equal(source.hogeFoo_bar)
    }

  }
   */

  describe("JSONStringOps#fromJSONString for Map objects") {

    it("converts JSON string value to Map object") {
      val source: Map[String, Any] = Map(
        "name" -> "name's length must be less than 32.",
        "something_like_that" -> "")

      val result: Try[Map[String, Any]] = JSONStringOps.fromJSONString[Map[String, String]](
        JSONStringOps.toJSONString(source), true)

      result should equal(Success(source))
    }

  }

}
