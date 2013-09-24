package skinny.validator

trait ValidatorLike {

  val validations: Validations

  lazy val params: Params = ParamsFromValidations(validations)

  lazy val errors: Errors = Errors(validations.toSeq.filter {
    result => result.isInstanceOf[ValidationFailure]
  }.groupBy(_.param.key).map {
    case (key, failures) => (key, failures.flatMap(_.errors))
  })

  def validate(): Boolean = !hasErrors

  def map[A](extractor: Params => A): Form[A] = {
    if (hasErrors) {
      Form(validations, None)
    } else {
      Form(validations, Option(extractor.apply(params)))
    }
  }

  def fold[A](errorsHandler: (Params, Errors) => A, paramsHandler: (Params) => A): A = {
    if (hasErrors) errorsHandler.apply(params, errors)
    else paramsHandler.apply(params)
  }

  def success[B](f: (Params) => B) = validations.success[B](f)

  def failure[B](f: (Params, Errors) => B) = validations.failure[B](f)

  def hasErrors: Boolean = !errors.isEmpty

  protected def extractValue(value: Any): Any = value match {
    case Some(v) => v
    case None => null
    case v => v
  }

}

