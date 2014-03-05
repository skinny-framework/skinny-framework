package skinny

/**
 * Strong parameter type definition.
 */
sealed trait ParamType

/**
 * Strong parameter type definition.
 */
object ParamType {

  case object Boolean extends ParamType
  case object Double extends ParamType
  case object Float extends ParamType
  case object Long extends ParamType
  case object Int extends ParamType
  case object Short extends ParamType
  case object BigDecimal extends ParamType
  case object String extends ParamType
  case object Byte extends ParamType
  case object ByteArray extends ParamType
  case object DateTime extends ParamType
  case object LocalDate extends ParamType
  case object LocalTime extends ParamType

}
