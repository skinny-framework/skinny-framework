import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import skinny.validator._

class UsageSpec extends FunSpec with ShouldMatchers {

  describe("Validator") {

    it("provides #validate for if/else statements") {
      val validator = Validator(
        param("user_id" -> 123) is notNull,
        param("name" -> null) is required & minLength(3)
      )

      if (validator.validate()) {
        // success
        fail("validation should be failed")
      } else {
        // error
        val (params, errors) = (validator.params, validator.errors)
        params.toMap should equal(Map("user_id" -> 123, "name" -> null))
        errors.get("user_id") should equal(Nil)
        val nameError = errors.get("name").head
        nameError.name should equal("required")
        nameError.messageParams should equal(Nil)
      }
    }

    it("provides #fold") {
      val validator = Validator(
        param("user_id" -> 123) is notNull,
        param("name" -> "seratch") is required & maxLength(3)
      )
      validator.fold[Any](
        (params, errors) => {
          // do something when errors are found
          params.toMap should equal(Map("user_id" -> 123, "name" -> "seratch"))
          errors.get("user_id") should equal(Nil)
          val nameError = errors.get("name").head
          nameError.name should equal("maxLength")
          nameError.messageParams.mkString(",") should equal("3")
        },
        (params) => {
          // do something when success
          fail("validation should be failed")
        }
      )
    }
  }

  describe("MapValidator") {
    it("provides #validate for if/else statements") {
      val params = Map("user_id" -> 123, "name" -> "seratch")
      val validator = MapValidator(params)(
        paramKey("user_id") is notNull,
        paramKey("name") is required & maxLength(3)
      )

      if (validator.validate()) {
        // success
        fail("validation should be failed")
      } else {
        // error
        val (params, errors) = (validator.params, validator.errors)
        params.toMap should equal(Map("user_id" -> 123, "name" -> "seratch"))
        errors.get("user_id") should equal(Nil)
        val nameError = errors.get("name").head
        nameError.name should equal("maxLength")
        nameError.messageParams.mkString(",") should equal("3")
      }
    }

    it("provides #fold") {
      val params = Map("user_id" -> 123, "name" -> "x")
      val validator = MapValidator(params)(
        paramKey("user_id") is notNull,
        paramKey("name") is required & minLength(3)
      )

      validator.fold[Any](
        (params, errors) => {
          // do something when errors are found
          params.toMap should equal(Map("user_id" -> 123, "name" -> "x"))
          errors.get("user_id") should equal(Nil)
          val nameError = errors.get("name").head
          nameError.name should equal("minLength")
          nameError.messageParams.mkString(",") should equal("3")
        },
        (params) => {
          // do something when success
          fail("validation should be failed")
        }
      )
    }
  }

}
