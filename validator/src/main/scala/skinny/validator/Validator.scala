package skinny.validator

/**
  * Validator factory.
  */
object Validator {

  /**
    * Creates a validator from a Map value.
    *
    * @param map Map value
    * @return validator
    */
  def apply(map: Map[String, Any]): MapValidator = {
    new MapValidator(map)
  }

  /**
    * Creates a validator from new validations.
    *
    * @param newValidations new validations
    * @return validator
    */
  def apply(newValidations: NewValidation*): Validator = {
    new Validator().apply(newValidations: _*)
  }

}

/**
  * Validator.
  *
  * @param validations validations
  */
class Validator(override val validations: Validations = Validations(Map(), Nil)) extends ValidatorLike {

  /**
    * Creates a validator from new validations.
    *
    * @param newValidations new validations
    * @return validator
    */
  def apply(newValidations: NewValidation*): Validator = {
    val mergedValidations = validations.statesAsSeq ++ newValidations.map {
      case NewValidation(kv: KeyValueParamDefinition, vs: ValidationRule) =>
        vs.apply(KeyValueParamDefinition(kv.key, extractRawValue(kv.value)))
      case done => done
    }
    new Validator(
      Validations(
        params = Map(mergedValidations.map { r =>
          (r.paramDef.key, r.paramDef.value)
        }: _*),
        states = mergedValidations
      )
    )
  }

}
