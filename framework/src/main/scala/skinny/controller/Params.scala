package skinny.controller

import scala.language.dynamics
import skinny._
import skinny.util.DateTimeUtil

object Params {

  val Year = "Year"
  val Month = "Month"
  val Day = "Day"
  val Hour = "Hour"
  val Minute = "Minute"
  val Second = "Second"

  val _year = "_year"
  val _month = "_month"
  val _day = "_day"
  val _hour = "_hour"
  val _minute = "_minute"
  val _second = "_second"
}

/**
 * Scalatra's params wrapper.
 *
 * @param underlying Scalatra's params
 */
case class Params(underlying: Map[String, Any]) extends Dynamic {

  import Params._

  /**
   * Permits parameters to be updated.
   *
   * @param paramKeyAndParamTypes name and param type
   * @return permitted parameters
   */
  def permit(paramKeyAndParamTypes: (String, ParamType)*): PermittedStrongParameters = {
    StrongParameters(underlying).permit(paramKeyAndParamTypes: _*)
  }

  def isSnakeCasedParams(keyPrefix: String): Boolean = {
    getAs[String](keyPrefix + Year)
      .orElse(getAs[String](keyPrefix + Month))
      .orElse(getAs[String](keyPrefix + Day))
      .orElse(getAs[String](keyPrefix + Hour))
      .orElse(getAs[String](keyPrefix + Minute))
      .orElse(getAs[String](keyPrefix + Second))
      .isEmpty
  }

  def toYmdKeys(key: String): (String, String, String) = {
    if (isSnakeCasedParams(key)) (key + _year, key + _month, key + _day)
    else (key + Year, key + Month, key + Day)
  }

  def toYmdhmsKeys(key: String): (String, String, String, String, String, String) = {
    if (isSnakeCasedParams(key)) (key + _year, key + _month, key + _day, key + _hour, key + _minute, key + _second)
    else (key + Year, key + Month, key + Day, key + Hour, key + Minute, key + Second)
  }

  def toHmsKeys(key: String): (String, String, String) = {
    if (isSnakeCasedParams(key)) (key + _hour, key + _minute, key + _second)
    else (key + Hour, key + Minute, key + Second)
  }

  /**
   * Appends a new Date param to params.
   *
   * @param ymd year,month,day keys
   * @param key new param key
   * @return params
   */
  def withDate(ymd: (String, String, String), key: String): Params = ymd match {
    case (year, month, day) =>
      DateTimeUtil.toUnsafeDateString(underlying, year, month, day).map { value =>
        Params(underlying + (key -> value))
      } getOrElse this
    case _ => this
  }

  /**
   * Appends a new Date param to params.
   *
   * @param key new param key
   * @return params
   */
  def withDate(key: String): Params = {
    withDate(toYmdKeys(key), key)
  }

  /**
   * Appends a new Date param to params.
   *
   * @param hms hour,minute,second keys
   * @param key new param key
   * @return params
   */
  def withTime(hms: (String, String, String), key: String): Params = hms match {
    case (hour, minute, second) => DateTimeUtil.toUnsafeTimeString(underlying, hour, minute, second).map { value =>
      Params(underlying + (key -> value))
    } getOrElse this
    case _ => this
  }

  /**
   * Appends a new Date param to params.
   *
   * @param key new param key
   * @return params
   */
  def withTime(key: String): Params = withDate(toHmsKeys(key), key)

  /**
   * Appends a new DateTime param to params.
   *
   * @param ymdhms year,month,day,hour,minute,second keys
   * @param key new param key
   * @return params
   */
  def withDateTime(ymdhms: (String, String, String, String, String, String), key: String): Params = ymdhms match {
    case (year, month, day, hour, minute, second) =>
      DateTimeUtil.toUnsafeDateTimeString(underlying, year, month, day, hour, minute, second).map { value =>
        Params(underlying + (key -> value))
      } getOrElse this
    case _ => this
  }

  /**
   * Appends a new DateTime param to params.
   *
   * @param key new param key
   * @return params
   */
  def withDateTime(key: String): Params = withDateTime(toYmdhmsKeys(key), key)

  /**
   * Returns value for the key if exists.
   *
   * @param key key
   * @tparam A type
   * @return value
   */
  def getAs[A](key: String): Option[A] = underlying.get(key).map(_.asInstanceOf[A])

  /**
   * Enables accessing key using type-dynamic. Both of the following code is same.
   *
   * {{{
   *   params.get("userId")
   *   params.userId
   * }}}
   *
   * @param key key
   * @return value if exists
   */
  def selectDynamic(key: String): Option[Any] = underlying.get(key).map {
    case Some(v) => v
    case None => null
    case v => v
  }

}
