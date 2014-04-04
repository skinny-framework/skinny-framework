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
    val ParamTypeExtractor = paramType
    Option(value).map { value =>
      value match {
        case Some(v) => getTypedValueFromStrongParameter(fieldName, v, paramType)
        case None => null
        case ParamTypeExtractor(v) => v
        case v: String if v == "" => null
        case v: String => v
        case v => throw new IllegalArgumentException(s"Cannot convert '${v}' to ${paramType} value.")
      }
    }
  }

}
