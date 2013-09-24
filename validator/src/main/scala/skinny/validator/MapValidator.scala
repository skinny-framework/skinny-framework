package skinny.validator

case class MapValidator(map: Map[String, Any] = Map(), override val validations: Validations = Validations(Map(), Nil)) extends ValidatorLike {

  override lazy val params: Params = ParamsFromMap(map)

  def apply(validations: NewValidation*): MapValidator = {
    val mutableMap = collection.mutable.Map(map.toSeq: _*)
    val newValidations = validations.toSeq ++ validations.map {
      case NewValidation(k: KeyParamDefinition, validations) =>
        if (!mutableMap.contains(k.key)) {
          mutableMap.update(k.key, extractValue(map.get(k.key)))
        }
        validations.apply(KeyValueParamDefinition(k.key, extractValue(map.get(k.key))))

      case NewValidation(kv: KeyValueParamDefinition, validations) =>
        if (!mutableMap.contains(kv.key)) {
          mutableMap.update(kv.key, kv.value)
        }
        validations.apply(KeyValueParamDefinition(kv.key, extractValue(kv.value)))

      case done => done
    }

    val newMap = Map(mutableMap.toSeq: _*)
    MapValidator(newMap, Validations(newMap, newValidations))
  }

}

