package skinny.validator

import java.util.Date
import skinny.validator.implicits.ParamsGetAsImplicits

sealed trait Params { self: ParamsGetAsImplicits =>

  protected val paramsMap: Map[String, Any]

  def keys(): Seq[String] = toSeq().map(_.key)

  def values(): Seq[Any] = toSeq().map(_.value)

  def toMap(): Map[String, Any] = paramsMap

  def toSeq(): Seq[ParamDefinition] = paramsMap.toSeq.map { case (k, v) => KeyValueParamDefinition(k, v) }

  def get(key: String): Option[String] = paramsMap.get(key).filterNot(_ == null).map(_.toString)

  def getAs[T <: Any](name: String)(implicit tc: TypeConverter[String, T]): Option[T] = get(name).flatMap(tc(_))

  def getAs[T <: Date](nameAndFormat: (String, String)): Option[Date] = {
    getAs(nameAndFormat._1)(skinnyValidatorStringToDate(nameAndFormat._2))
  }

  def getAsOrElse[T <: Any](name: String, default: => T)(implicit tc: TypeConverter[String, T]): T = {
    getAs[T](name).getOrElse(default)
  }

  def getAsOrElse(nameAndFormat: (String, String), default: => Date)(implicit tc: TypeConverter[String, Date]): Date = {
    getAs[Date](nameAndFormat).getOrElse(default)
  }

}

case class ParamsFromValidations(validations: Validations) extends Params with ParamsGetAsImplicits {
  override protected val paramsMap: Map[String, Any] = validations.toMap()
}

case class ParamsFromMap(map: Map[String, Any]) extends Params with ParamsGetAsImplicits {
  override protected val paramsMap: Map[String, Any] = map
}

