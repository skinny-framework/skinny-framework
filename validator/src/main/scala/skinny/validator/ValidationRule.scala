package skinny.validator

/**
 * Validation rule.
 */
trait ValidationRule extends ((KeyValueParamDefinition) => ValidationState) with Error {

  /**
   * Validation itself.
   *
   * @param value any value
   * @return valid if true
   */
  def isValid(value: Any): Boolean

  /**
   * Applies this validation rule to parameter.
   *
   * @param paramDef param definition
   * @return validation
   */
  def apply(paramDef: KeyValueParamDefinition): ValidationState = {
    if (isValid(paramDef.value)) ValidationSuccess(paramDef)
    else ValidationFailure(paramDef, Seq(this))
  }

  /**
   * Adds new and condition.
   *
   * @param that new validation rule
   * @return combined validation rule
   */
  def and(that: ValidationRule): ValidationRule = &(that)

  /**
   * Adds new and condition.
   *
   * @param that new validation rule
   * @return combined validation rule
   */
  def &(that: ValidationRule): ValidationRule = {
    val _this = this
    new Object with ValidationRule {

      def name: String = "combined-results"
      def isValid(value: Any): Boolean = throw new IllegalStateException

      override def apply(paramDef: KeyValueParamDefinition): ValidationState = {
        _this.apply(paramDef) match {
          case _: ValidationSuccess => that.apply(paramDef)
          case f: ValidationFailure => f
          case _ => throw new IllegalStateException
        }
      }

    }
  }

}

