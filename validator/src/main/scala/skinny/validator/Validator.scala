package skinny.validator

object Validator {

  def apply(map: Map[String, Any]): MapValidator = {
    new MapValidator(map)
  }

  def apply(validations: NewValidation*): Validator = {
    new Validator().apply(validations: _*)
  }

}

class Validator(override val validations: Validations = Validations(Map(), Nil)) extends ValidatorLike {

  def apply(newValidations: NewValidation*): Validator = {
    val mergedValidations = validations.toSeq ++ newValidations.map {
      case NewValidation(kv: KeyValueParamDefinition, vs: ValidationRule) =>
        vs.apply(KeyValueParamDefinition(kv.key, extractValue(kv.value)))
      case done => done
    }
    new Validator(Validations(
      params = Map(mergedValidations.map { r => (r.param.key, r.param.value) }: _*),
      validations = mergedValidations
    ))
  }

}

