package skinny.validator

/**
  * Results projection.
  */
object ResultsProjection {

  val defaultOnSuccess: (Parameters) => Nothing = { (params: Parameters) =>
    throw new IllegalStateException("onSuccess handler is not specified.")
  }

  val defaultOnFailures: (Parameters, Errors) => Nothing = { (params: Parameters, errors: Errors) =>
    throw new IllegalStateException("onFailures handler is not specified.")
  }

}

/**
  * Results projection.
  *
  * @tparam A result type
  */
sealed trait ResultsProjection[+A] {

  val results: Validations

  val onSuccess: (Parameters) => A

  val onFailures: (Parameters, Errors) => A

  def success[B >: A](f: (Parameters) => B): SuccessesProjection[B] = {
    SuccessesProjection(results, onSuccess, onFailures).map(f)
  }

  def failure[B >: A](f: (Parameters, Errors) => B): FailuresProjection[B] = {
    FailuresProjection(results, onSuccess, onFailures).map(f)
  }

  def apply(): A = {
    if (results.isSuccess) onSuccess(results.params)
    else onFailures(results.params, results.errors)
  }

}

/**
  * Successes projection.
  *
  * @param results results
  * @param onSuccess success event handler
  * @param onFailures failure event handler
  * @tparam A result type
  */
case class SuccessesProjection[+A](
    override val results: Validations,
    override val onSuccess: (Parameters) => A,
    override val onFailures: (Parameters, Errors) => A
) extends ResultsProjection[A] {

  def map[B >: A](f: (Parameters) => B): SuccessesProjection[B] = {
    SuccessesProjection[B](results, f, onFailures.asInstanceOf[(Parameters, Errors) => B])
  }

}

/**
  * Failures projection.
  *
  * @param results results
  * @param onSuccess success event handler
  * @param onFailures failure event handler
  * @tparam A result type
  */
case class FailuresProjection[+A](
    override val results: Validations,
    override val onSuccess: (Parameters) => A,
    override val onFailures: (Parameters, Errors) => A
) extends ResultsProjection[A] {

  def map[B >: A](f: (Parameters, Errors) => B): FailuresProjection[B] = {
    FailuresProjection[B](results, onSuccess.asInstanceOf[(Parameters) => B], f)
  }

}
