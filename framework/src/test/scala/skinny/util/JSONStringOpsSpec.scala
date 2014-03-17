package skinny.util

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.Serializable

// http://www.playframework.com/documentation/2.2.x/ScalaJson
case class UserResponse(user: User)
case class User(name: String, age: Int, email: String, isAlive: Boolean = false, friend: Option[User] = None)

class JSONStringOpsSpec extends FunSpec with ShouldMatchers {

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
      userResponse.isDefined should be(true)

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

  describe("JSONStringOps#fromJSONString for Map objects") {

    it("converts JSON string value to Map object") {
      val source: Map[String, Any] = Map(
        "name" -> "name's length must be less than 32.",
        "something_like_that" -> "")

      val result: Option[Map[String, Any]] = JSONStringOps.fromJSONString[Map[String, String]](
        JSONStringOps.toJSONString(source), true)

      result.get should equal(source)
    }

  }

}
