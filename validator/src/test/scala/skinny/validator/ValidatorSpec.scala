package skinny.validator

import org.scalatest._
import skinny.validator.implicits.ParametersGetAsImplicits

class ValidatorSpec extends FlatSpec with Matchers with ParametersGetAsImplicits {

  behavior of "Validator"

  it should "pass all the valid values" in {
    val validator = Validator(
      param("id" -> 12345) is notNull,
      param("first_name" -> "Kaz") is required & minLength(3),
      param("last_name" -> "Sera") is checkAll(minLength(1), maxLength(5))
    )

    val res1: Int = validator.success {
      inputs => inputs.getAs[Int]("id").getOrElse(-1)
    }.failure {
      (inputs, errors) => -1
    }.apply()
    res1 should equal(12345)

    val res2: Int = validator.fold(
      (inputs: Parameters, errors: Errors) => -1,
      (inputs: Parameters) => inputs.getAs[Int]("id").getOrElse(-1)
    )
    res2 should equal(12345)

    validator.errors.size should equal(0)
  }

  it should "find invalid values" in {
    val validator = Validator(
      param("id" -> 12345) is notNull & maxLength(4), // 1 error
      param("first_name" -> "") is required & minLength(3), // 1 error
      param("last_name" -> "Sera") is checkAll(required, minLength(5), numeric), // 2 errors
      param("gender" -> "male") is required
    )

    val res: String = validator.success {
      inputs => "Success!"
    }.failure {
      (inputs, errors) => "Failed!"
    }.apply()
    res should equal("Failed!")

    validator.errors.size should equal(3)
    validator.errors.toMap.toSeq.flatMap(_._2).size should equal(4)
  }

  it should "define and alias" in {
    val validator = Validator(
      param("id" -> 12345) is (notNull and maxLength(4)), // 1 error
      param("first_name" -> "") is (required and minLength(3)) // 1 error
    )

    val res: String = validator.success {
      inputs => "Success!"
    }.failure {
      (inputs, errors) => "Failed!"
    }.apply()
    res should equal("Failed!")

    validator.errors.size should equal(2)
  }

  it should "be able to use #checkAll" in {
    val v = Validator(
      param("id" -> "aa") is (checkAll(required, numeric, minLength(3)))
    )
    v.errors.get("id").size should equal(2)
  }

  it should "accept Option input values" in {
    val params = Map("id" -> 12345, "name" -> "Sera")
    val v = Validator(
      param("id" -> params.get("id")) is notNull & maxLength(4),
      param("name" -> params.get("name")) is required & minLength(5),
      param("gender" -> params.get("gender")) is required
    )
    v.errors.size should equal(3)
  }

  it should "accept Map value" in {
    val params = Map("password" -> "", "newPassword" -> "123456", "reNewPassword" -> "2345")
    val v = Validator(params)(
      paramKey("password") is required,
      paramKey("newPassword") is required & minLength(6),
      paramKey("reNewPassword") is required & minLength(6),
      param("pair" -> (params("newPassword"), params("reNewPassword"))) are same
    )
    v.errors.size should equal(3)
  }

  object AuthService {
    def authenticate(username: String, password: String) = username == password
  }

  case object authenticated extends ValidationRule {
    def name = "authenticated"

    def isValid(v: Any) = {
      val (username, password) = v.asInstanceOf[(String, String)]
      AuthService.authenticate(username, password)
    }
  }

  it should "perform authentication failure" in {
    Validator()(
      param("login" -> ("id", "pass")) is authenticated
    ).failure {
        (inputs, errors) =>
          errors.get("login").head.name == "authenticated"
      }.success {
        inputs => fail()
      }.apply()
  }

  it should "perform authentication success" in {
    val results = Validator()(
      param("login" -> ("idpass", "idpass")) is authenticated
    ).success {
        inputs =>
      }.failure {
        (inputs, errors) => fail()
      }.apply()
  }

  it should "apply several times (success)" in {
    Validator
      .apply(param("a", "aa") is required)
      .apply(param("b", "bb") is required)
      .success {
        inputs =>
          inputs.getAs[String]("a").get should equal("aa")
          inputs.getAs[String]("b").get should equal("bb")
      }.failure {
        (inputs, errors) =>
          fail()
      }.apply()

    Validator(Map("a" -> 1, "b" -> 2, "c" -> 3))
      .apply(paramKey("a") is required)
      .apply(paramKey("b") is required)
      .success {
        inputs =>
          inputs.getAs[Int]("a").get should equal(1)
          inputs.getAs[Int]("b").get should equal(2)
          inputs.getAs[Int]("c").get should equal(3)
      }.failure {
        (inputs, errors) =>
          fail()
      }.apply()
  }

  it should "apply several times (failure)" in {
    Validator
      .apply(param("a", null) is required)
      .apply(param("b", "") is required)
      .apply(param("c", "cc") is required)
      .success {
        inputs =>
          fail()
      }.failure {
        (inputs, errors) =>
          inputs.get("a") should equal(None)
          inputs.get("b") should equal(Some(""))
          inputs.get("c") should equal(Some("cc"))
      }.apply()

    Validator(Map("a" -> null, "b" -> "", "c" -> "cc"))
      .apply(paramKey("a") is required)
      .apply(paramKey("b") is required)
      .success {
        inputs =>
          fail()
      }.failure {
        (inputs, errors) =>
          inputs.get("a") should equal(None)
          inputs.get("b") should equal(Some(""))
          inputs.get("c") should equal(Some("cc"))
      }.apply()
  }

  it should "apply key-lacked Map value" in {
    Validator(Map("first" -> "aaa"))
      .apply(paramKey("first") is required)
      .apply(paramKey("second") is required)
      .success {
        inputs =>
          fail()
      }.failure {
        (inputs, errors) =>
          inputs.keys().size should equal(2)
          inputs.get("first") should equal(Some("aaa"))
          inputs.get("second") should equal(None)
      }.apply()
  }

}
