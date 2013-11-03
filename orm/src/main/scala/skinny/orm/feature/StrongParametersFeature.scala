package skinny.orm.feature

import org.joda.time._
import skinny.ParamType
import skinny.util.DateTimeUtil

/**
 * Strong parameters support.
 */
trait StrongParametersFeature {

  /**
   * Returns typed value from a strong parameter.
   *
   * @param fieldName field name
   * @param value actual value
   * @param paramType param type definition
   * @return typed value if exists
   */
  protected def getTypedValueFromStrongParameter(fieldName: String, value: Any, paramType: ParamType): Option[Any] = {
    Option(value).map { value =>
      value match {
        case Some(v) => getTypedValueFromStrongParameter(fieldName, v, paramType)
        case None => null
        case v: Boolean if paramType == ParamType.Boolean => v
        case v: Double if paramType == ParamType.Double => v
        case v: Float if paramType == ParamType.Float => v
        case v: Int if paramType == ParamType.Int => v
        case v: Long if paramType == ParamType.Long => v
        case v: Short if paramType == ParamType.Short => v
        case v: Byte if paramType == ParamType.Byte => v
        case v: Array[Byte] if paramType == ParamType.ByteArray => v
        case v: java.util.Date if paramType == ParamType.DateTime => v
        case v: DateTime if paramType == ParamType.DateTime => v
        case v: LocalDate if paramType == ParamType.LocalDate => v
        case v: LocalTime if paramType == ParamType.LocalTime => v
        case v: String if v == "" => null
        case v: String =>
          paramType match {
            case ParamType.Boolean => v.toBoolean
            case ParamType.Byte => v.toByte
            case ParamType.Double => v.toDouble
            case ParamType.Float => v.toFloat
            case ParamType.Int => v.toInt
            case ParamType.Long => v.toLong
            case ParamType.Short => v.toShort
            case ParamType.String => v
            case ParamType.ByteArray => v.getBytes
            case ParamType.DateTime => DateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.DateTime))
            case ParamType.LocalDate => LocalDate.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalDate))
            case ParamType.LocalTime => LocalTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalTime))
            case v => v
          }
        case v => throw new IllegalArgumentException(s"Cannot convert '${v}' to ${paramType} value.")
      }
    }
  }

}
