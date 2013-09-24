package skinny.validator

object Validations {
  def apply(params: Map[String, Any], validations: Seq[Validation]): Validations = {
    val mutableMap = collection.mutable.Map(params.toSeq: _*)
    val processedValidations = validations.map {
      case NewValidation(k: KeyParamDefinition, validations) =>
        if (!mutableMap.contains(k.key)) {
          mutableMap.update(k.key, extractValue(params.get(k.key)))
        }
        validations.apply(KeyValueParamDefinition(k.key, extractValue(params.get(k.key))))

      case NewValidation(kv: KeyValueParamDefinition, validations) =>
        if (!mutableMap.contains(kv.key)) {
          mutableMap.update(kv.key, kv.value)
        }
        validations.apply(KeyValueParamDefinition(kv.key, extractValue(kv.value)))

      case done => done
    }
    new ValidationsImpl(params, processedValidations)
  }

  private[this] def extractValue(value: Any): Any = value match {
    case Some(v) => v
    case None => null
    case v => v
  }
}

trait Validations {
  val paramsMap: Map[String, Any]
  val validations: Seq[Validation]

  def params: Params = ParamsFromMap(paramsMap)

  def isSuccess: Boolean = filterFailuresOnly().isEmpty
  def errors: Errors = Errors(filterErrorsOnly())

  def filterSuccessesOnly(): Seq[ValidationSuccess] = validations.filter(r => r.isInstanceOf[ValidationSuccess]).map(r => r.asInstanceOf[ValidationSuccess])
  def filterFailuresOnly(): Seq[ValidationFailure] = validations.filter(r => r.isInstanceOf[ValidationFailure]).map(r => r.asInstanceOf[ValidationFailure])
  def filterErrorsOnly(): Map[String, Seq[Error]] = filterFailuresOnly().groupBy(_.param.key).map {
    case (key, fs) => (key, fs.flatMap(_.errors))
  }

  def success[A](f: (Params) => A): SuccessesProjection[A] = {
    SuccessesProjection[A](this, ResultsProjection.defaultOnSuccess, ResultsProjection.defaultOnFailures).map(f)
  }
  def failure[A](f: (Params, Errors) => A): FailuresProjection[A] = {
    FailuresProjection[A](this, ResultsProjection.defaultOnSuccess, ResultsProjection.defaultOnFailures).map(f)
  }

  def toSeq(): Seq[Validation] = validations
  def toMap(): Map[String, Any] = Map(validations.map { r => (r.param.key, r.param.value) }: _*)
}

private class ValidationsImpl(
  override val paramsMap: Map[String, Any], override val validations: Seq[Validation])
    extends Validations

