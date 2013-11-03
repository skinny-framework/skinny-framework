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
  def withDateValue(ymd: (String, String, String), key: String): Params = {
    Params(underlying + (key -> DateTimeUtil.toDateString(underlying, ymd._1, ymd._2, ymd._3)))
  }

  /**
   * Appends a new Date param to params.
   *
   * @param hms hour,minute,second keys
   * @param key new param key
   * @return params
   */
  def withTimeValue(hms: (String, String, String), key: String): Params = {
    Params(underlying + (key -> DateTimeUtil.toTimeString(underlying, hms._1, hms._2, hms._3)))
  }

  /**
   * Appends a new DateTime param to params.
   *
   * @param ymdhms year,month,day,hour,minute,second keys
   * @param key new param key
   * @return params
   */
  def withDateTimeValue(ymdhms: (String, String, String, String, String, String), key: String): Params = {
    Params(underlying + (key -> DateTimeUtil.toDateTimeString(underlying,
      ymdhms._1, ymdhms._2, ymdhms._3, ymdhms._4, ymdhms._5, ymdhms._6)))
  }

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
  def selectDynamic(key: String): Option[Any] = underlying.get(key).map { v =>
    // #toString is work around for issue #11 
    // 'v' should not be an `Any` value because 1234: Any will be converted to '1,234'.
    v match {
      case Some(v) => v.toString
      case None => null
      case v => v.toString
    }
  }

}
