package skinny.validator

/**
 * Results projection.
 */
object ResultsProjection {

  val defaultOnSuccess: (Params) => Nothing = {
    (params: Params) => throw new IllegalStateException("onSuccess handler is not specified.")
  }

  val defaultOnFailures: (Params, Errors) => Nothing = {
    (params: Params, errors: Errors) => throw new IllegalStateException("onFailures handler is not specified.")
  }

}

/**
 * Results projection.
 *
 * @tparam A result type
 */
sealed trait ResultsProjection[+A] {

  val results: Validations

  val onSuccess: (Params) => A

  val onFailures: (Params, Errors) => A

  def success[B >: A](f: (Params) => B): SuccessesProjection[B] = {
    SuccessesProjection(results, onSuccess, onFailures).map(f)
  }

  def failure[B >: A](f: (Params, Errors) => B): FailuresProjection[B] = {
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
case class SuccessesProjection[+A](override val results: Validations,
    override val onSuccess: (Params) => A,
    override val onFailures: (Params, Errors) => A) extends ResultsProjection[A] {

  def map[B >: A](f: (Params) => B): SuccessesProjection[B] = {
    SuccessesProjection[B](results, f, onFailures.asInstanceOf[(Params, Errors) => B])
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
case class FailuresProjection[+A](override val results: Validations,
    override val onSuccess: (Params) => A,
    override val onFailures: (Params, Errors) => A) extends ResultsProjection[A] {

  def map[B >: A](f: (Params, Errors) => B): FailuresProjection[B] = {
    FailuresProjection[B](results, onSuccess.asInstanceOf[(Params) => B], f)
  }

}

