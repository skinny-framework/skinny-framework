package skinny.validator

/**
  * Validator like feature.
  */
trait ValidatorLike {

  /**
    * Underlying validations.
    */
  val validations: Validations

  /**
    * Params
    */
  lazy val params: Parameters = ParametersFromValidations(validations)

  /**
    * Errors
    */
  lazy val errors: Errors = Errors(
    validations.statesAsSeq
      .filter { result =>
        result.isInstanceOf[ValidationFailure]
      }
      .groupBy(_.paramDef.key)
      .map {
        case (key, failures) => (key, failures.flatMap(_.errors).distinct)
      }
  )

  /**
    * Executes validation.
    *
    * @return valid if true
    */
  def validate(): Boolean = !hasErrors

  /**
    * Extract a form from params.
    *
    * @param extractor extractor
    * @tparam A form response type
    * @return form
    */
  def map[A](extractor: Parameters => A): Form[A] = {
    if (hasErrors) {
      Form(validations, None)
    } else {
      Form(validations, Option(extractor.apply(params)))
    }
  }

  /**
    * Fold operation.
    *
    * @param failureHandler failure handler
    * @param successHandler success handler
    * @tparam A return type
    * @return result
    */
  def fold[A](failureHandler: (Parameters, Errors) => A, successHandler: (Parameters) => A): A = {
    if (hasErrors) failureHandler.apply(params, errors)
    else successHandler.apply(params)
  }

  /**
    * Success event handler.
    *
    * @param f operation
    * @tparam B extracted value type
    * @return projection
    */
  def success[B](f: (Parameters) => B) = validations.success[B](f)

  /**
    * Failure event handler.
    *
    * @param f operation
    * @tparam B extracted value type
    * @return projection
    */
  def failure[B](f: (Parameters, Errors) => B) = validations.failure[B](f)

  /**
    * Errors exist if true.
    *
    * @return true if errors exist
    */
  def hasErrors: Boolean = !errors.isEmpty

  /**
    * Extracts value from optional value.
    *
    * @param value optional value
    * @return raw value
    */
  protected def extractRawValue(value: Any): Any = value match {
    case Some(v) => v
    case None    => null
    case v       => v
  }

}
