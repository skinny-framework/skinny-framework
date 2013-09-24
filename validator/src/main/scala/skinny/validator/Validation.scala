package skinny.validator

sealed trait Validation {

  val param: ParamDefinition
  val errors: Seq[Error] = Nil

  def isDone: Boolean = true
  def isSuccess: Boolean = true
  def isFailure: Boolean = !isSuccess

  def success: ValidationSuccess = this.asInstanceOf[ValidationSuccess]
  def failure: ValidationFailure = this.asInstanceOf[ValidationFailure]

  def toEither: Either[ValidationFailure, ValidationSuccess]

  def toSuccessOption: Option[ValidationSuccess] = None
  def toFailureOption: Option[ValidationFailure] = None

}

case class NewValidation(override val param: ParamDefinition, validations: ValidationRule) extends Validation {
  override def isDone: Boolean = false
  def toEither: Either[ValidationFailure, ValidationSuccess] = throw new IllegalStateException
}

case class ValidationSuccess(override val param: ParamDefinition) extends Validation {
  def toEither: Either[ValidationFailure, ValidationSuccess] = Right(this)
  override def toSuccessOption: Option[ValidationSuccess] = Some(this)
}

case class ValidationFailure(override val param: ParamDefinition, override val errors: Seq[Error]) extends Validation {
  override def isSuccess: Boolean = false
  def toEither: Either[ValidationFailure, ValidationSuccess] = Left(this)
  override def toFailureOption: Option[ValidationFailure] = Some(this)
}

