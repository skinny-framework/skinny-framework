package skinny.validator

/**
  * Form object.
  *
  * @param validations validations
  * @param value value if exists
  * @tparam A raw value type
  */
case class Form[A](validations: Validations, private val value: Option[A]) extends ValidatorLike {

  /**
    * Returns raw value if exists.
    *
    * @return raw value
    */
  def get: A = value.get

}
