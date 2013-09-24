package skinny.validator

case class Form[A](validations: Validations, private val value: Option[A]) extends ValidatorLike {
  def get: A = value.get
}
