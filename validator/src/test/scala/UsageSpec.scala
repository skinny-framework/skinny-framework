import org.scalatest._
import skinny.validator._

class UsageSpec extends FunSpec with Matchers {

  describe("Validator") {

    def simpleValidator1(userId: Int, name: String): Validator = Validator(
      param("user_id" -> userId) is notNull & numeric & intValue,
      param("name"    -> name) is required & minLength(3)
    )

    it("should provide #validate for if/else statements") {
      val validator = simpleValidator1(123, null)
      if (validator.validate()) {
        // -----------------------
        // success
        fail("validation should be failed")
      } else {
        // -----------------------
        // validation errors

        // can access parameters via #params
        val params: Parameters = validator.params
        params.toMap should equal(Map("user_id" -> 123, "name" -> null))
        params.keys() should (equal(Seq("user_id", "name")) or equal(Seq("name", "user_id")))
        params.values() should (equal(Seq(123, null)) or equal(Seq(null, 123)))

        // can access errors
        val errors: Errors = validator.errors
        // user_id is valid
        errors.get("user_id") should equal(Nil)
        // name is invalid and one error is found
        val errorsForName: Seq[Error] = errors.get("name")
        errorsForName.head should equal(Error("required", Nil))
      }
    }

    def simpleValidator2(userId: Int, name: String): Validator = Validator(
      param("user_id" -> userId) is notNull & numeric & intValue,
      param("name"    -> name) is required & maxLength(3)
    )

    it("should provide #fold") {
      val validator = simpleValidator2(123, "seratch")
      validator.fold[Any](
        (params: Parameters, errors: Errors) => {
          // -----------------------
          // validation errors
          errors.get("user_id") should equal(Nil)
          errors.get("name") should equal(Seq(Error("maxLength", Seq("3"))))
        },
        (params: Parameters) => {
          // -----------------------
          // success
          fail("validation should be failed")
        }
      )
    }
  }

  describe("MapValidator") {

    def mapValidator1(params: Map[String, Any]): MapValidator = MapValidator(params)(
      paramKey("user_id") is notNull,
      paramKey("name") is required & maxLength(3)
    )

    it("should provide #validate for if/else statements") {
      val params    = Map("user_id" -> 123, "name" -> "seratch")
      val validator = mapValidator1(params)
      if (validator.validate()) {
        // -----------------------
        // success
        fail("validation should be failed")
      } else {
        // -----------------------
        // validation errors
        validator.params.toMap should equal(Map("user_id" -> 123, "name" -> "seratch"))
        validator.errors.get("user_id") should equal(Nil)
        validator.errors.get("name") should equal(Seq(Error("maxLength", Seq("3"))))
      }
    }

    def mapValidator2(params: Map[String, Any]): MapValidator = MapValidator(params)(
      paramKey("user_id") is notNull,
      paramKey("name") is required & minLength(3)
    )

    it("should provide #fold") {
      val params    = Map("user_id" -> 123, "name" -> "x")
      val validator = mapValidator2(params)
      validator.fold[Any](
        (params, errors) => {
          // -----------------------
          // validation errors
          params.toMap should equal(Map("user_id" -> 123, "name" -> "x"))
          errors.get("user_id") should equal(Nil)
          errors.get("name") should equal(Seq(Error("minLength", Seq("3"))))
        },
        (params) => {
          // -----------------------
          // success
          fail("validation should be failed")
        }
      )
    }
  }

  describe("Messages") {
    it("should be available") {
      val messages: Messages = Messages.loadFromConfig()
      messages.get("required", Seq("name")) should equal(Some("name is required"))
      messages.get("required", "name") should equal(Some("name is required")) // String is also a Seq
      messages.get("minLength", Seq("password", 6)) should equal(
        Some("password length must be greater than or equal to 6")
      )
    }
  }

}
