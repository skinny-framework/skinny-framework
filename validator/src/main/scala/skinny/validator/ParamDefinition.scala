package skinny.validator

/**
  * Param definition.
  */
sealed trait ParamDefinition {
  val key: String
  val value: Any
}

/**
  * Key param definition.
  *
  * @param key key
  */
case class OnlyKeyParamDefinition(override val key: String) extends ParamDefinition {
  override lazy val value: Any = throw new IllegalStateException
}

/**
  * Key and value param definition.
  *
  * @param key key
  * @param value value
  */
case class KeyValueParamDefinition(override val key: String, override val value: Any) extends ParamDefinition
