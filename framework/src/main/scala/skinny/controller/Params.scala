package skinny.controller

import scala.language.dynamics
import skinny._
import skinny.util.DateTimeUtil

/**
 * Scalatra's params wrapper.
 *
 * @param underlying Scalatra's params
 */
case class Params(underlying: Map[String, Any]) extends Dynamic {

  /**
   * Permits parameters to be updated.
   *
   * @param paramKeyAndParamTypes name and param type
   * @return permitted parameters
   */
  def permit(paramKeyAndParamTypes: (String, ParamType)*): PermittedStrongParameters = {
    StrongParameters(underlying).permit(paramKeyAndParamTypes: _*)
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
  def withDate(key: String): Params = withDate((key + "Year", key + "Month", key + "Day"), key)

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
  def withTime(key: String): Params = withDate((key + "Hour", key + "Minute", key + "Second"), key)

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
  def withDateTime(key: String): Params = withDateTime(
    (key + "Year", key + "Month", key + "Day", key + "Hour", key + "Minute", key + "Second"),
    key)

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
    // #toString is work around for issue #11
    // 'v' should not be an `Any` value because 1234: Any will be converted to '1,234'.
    case Some(true) => true
    case Some(false) => false
    case Some(v) => v.toString
    case None => null
    case true => true
    case false => false
    case v => v.toString
  }

}
