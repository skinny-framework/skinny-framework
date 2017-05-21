package skinny.validator

/**
  * Validation object which has param definition and validation results
  */
sealed trait ValidationState {

  val paramDef: ParamDefinition
  val errors: Seq[Error] = Nil

  def isDone: Boolean    = true
  def isSuccess: Boolean = true
  def isFailure: Boolean = !isSuccess

  def success: ValidationSuccess = this.asInstanceOf[ValidationSuccess]
  def failure: ValidationFailure = this.asInstanceOf[ValidationFailure]

  def toEither: Either[ValidationFailure, ValidationSuccess]

  def toSuccessOption: Option[ValidationSuccess] = None
  def toFailureOption: Option[ValidationFailure] = None

}

/**
  * Newly created validation which hasn't be applied yet.
  *
  * @param paramDef param definition
  * @param validations validation rules
  */
case class NewValidation(override val paramDef: ParamDefinition, validations: ValidationRule) extends ValidationState {

  def toEither: Either[ValidationFailure, ValidationSuccess] = throw new IllegalStateException

  override def isDone: Boolean = false
}

/**
  * Success.
  *
  * @param paramDef param definition
  */
case class ValidationSuccess(override val paramDef: ParamDefinition) extends ValidationState {

  def toEither: Either[ValidationFailure, ValidationSuccess] = Right(this)

  override def toSuccessOption: Option[ValidationSuccess] = Some(this)
}

/**
  * Failure.
  *
  * @param paramDef param definition
  * @param errors errors
  */
case class ValidationFailure(override val paramDef: ParamDefinition, override val errors: Seq[Error])
    extends ValidationState {

  def toEither: Either[ValidationFailure, ValidationSuccess] = Left(this)

  override def isSuccess: Boolean                         = false
  override def toFailureOption: Option[ValidationFailure] = Some(this)
}
