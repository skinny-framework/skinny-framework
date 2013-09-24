package skinny.validator

sealed trait ParamDefinition {
  val key: String
  val value: Any
}

case class KeyParamDefinition(override val key: String) extends ParamDefinition {
  override lazy val value: Any = throw new IllegalStateException
}

case class KeyValueParamDefinition(override val key: String, override val value: Any) extends ParamDefinition

