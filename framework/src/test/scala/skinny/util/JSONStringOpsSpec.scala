package skinny.util

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

// http://www.playframework.com/documentation/2.2.x/ScalaJson
case class UserResponse(user: User)
case class User(name: String, age: Int, email: String, isAlive: Boolean = false, friend: Option[User] = None)

class JSONStringOpsSpec extends FunSpec with ShouldMatchers {

  describe("JSONStringOps#fromJSONString") {

    it("parse Play2 documentation example") {
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

}
