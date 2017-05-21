package skinny.validator

import java.util.Date
import skinny.validator.implicits.ParametersGetAsImplicits

/**
  * Params
  */
sealed trait Parameters { self: ParametersGetAsImplicits =>

  protected val parametersMap: Map[String, Any]

  def keys(): Seq[String] = toSeq().map(_.key)

  def values(): Seq[Any] = toSeq().map(_.value)

  def toMap(): Map[String, Any] = parametersMap

  def toSeq(): Seq[ParamDefinition] = parametersMap.toSeq.map { case (k, v) => KeyValueParamDefinition(k, v) }

  def get(key: String): Option[String] = parametersMap.get(key).filterNot(_ == null).map(_.toString)

  def getAs[T <: Any](name: String)(implicit tc: ParamValueTypeConverter[String, T]): Option[T] =
    get(name).flatMap(tc(_))

  def getAs[T <: Date](nameAndFormat: (String, String)): Option[Date] = {
    getAs(nameAndFormat._1)(skinnyValidatorStringToDate(nameAndFormat._2))
  }

  def getAsOrElse[T <: Any](name: String, default: => T)(implicit tc: ParamValueTypeConverter[String, T]): T = {
    getAs[T](name).getOrElse(default)
  }

  def getAsOrElse(nameAndFormat: (String, String),
                  default: => Date)(implicit tc: ParamValueTypeConverter[String, Date]): Date = {
    getAs[Date](nameAndFormat).getOrElse(default)
  }

}

/**
  * Params from validations.
  *
  * @param validations validations
  */
case class ParametersFromValidations(validations: Validations) extends Parameters with ParametersGetAsImplicits {

  override protected val parametersMap: Map[String, Any] = validations.statesAsMap()
}

/**
  * Params from a Map value.
  *
  * @param map Map value
  */
case class ParametersFromMap(map: Map[String, Any]) extends Parameters with ParametersGetAsImplicits {

  override protected val parametersMap: Map[String, Any] = map
}
