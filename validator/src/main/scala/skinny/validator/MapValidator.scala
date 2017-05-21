package skinny.validator

/**
  * Validator which accepts Map value as inputs.
  *
  * @param paramMap param as a Map value
  * @param validations registered validations
  */
case class MapValidator(
    paramMap: Map[String, Any] = Map(),
    override val validations: Validations = Validations(Map(), Nil)
) extends ValidatorLike {

  override lazy val params: Parameters = ParametersFromMap(paramMap)

  /**
    * Applies new validations to inputs.
    *
    * @param validations validations
    * @return validator
    */
  def apply(validations: NewValidation*): MapValidator = {
    val mutableMap = collection.mutable.Map(paramMap.toSeq: _*)
    val newValidations = validations.toSeq ++ validations.map {
      case NewValidation(k: OnlyKeyParamDefinition, validations) =>
        if (!mutableMap.contains(k.key)) {
          mutableMap.update(k.key, extractRawValue(paramMap.get(k.key)))
        }
        validations.apply(KeyValueParamDefinition(k.key, extractRawValue(paramMap.get(k.key))))

      case NewValidation(kv: KeyValueParamDefinition, validations) =>
        if (!mutableMap.contains(kv.key)) {
          mutableMap.update(kv.key, kv.value)
        }
        validations.apply(KeyValueParamDefinition(kv.key, extractRawValue(kv.value)))

      case done => done
    }

    val newMap = Map(mutableMap.toSeq: _*)
    MapValidator(newMap, Validations(newMap, newValidations))
  }

}
