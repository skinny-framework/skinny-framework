package skinny.validator

trait ValidationRule extends Function[KeyValueParamDefinition, Validation] with Error {

  def isValid(value: Any): Boolean

  def apply(paramDef: KeyValueParamDefinition): Validation = {
    if (isValid(paramDef.value)) ValidationSuccess(paramDef)
    else ValidationFailure(paramDef, Seq(this))
  }

  def and(that: ValidationRule): ValidationRule = &(that)

  def &(that: ValidationRule): ValidationRule = {
    val _this = this
    new Object with ValidationRule {

      def name: String = "combined-results"
      def isValid(value: Any): Boolean = throw new IllegalStateException

      override def apply(paramDef: KeyValueParamDefinition): Validation = {
        _this.apply(paramDef) match {
          case _: ValidationSuccess => that.apply(paramDef)
          case f: ValidationFailure => f
          case _ => throw new IllegalStateException
        }
      }

    }
  }

}

