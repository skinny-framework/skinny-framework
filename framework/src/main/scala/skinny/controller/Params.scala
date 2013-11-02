package skinny.controller

import scala.language.dynamics

/**
 * Scalatra's params wrapper.
 *
 * @param underlying Scalatra's params
 */
case class Params(underlying: Map[String, Any]) extends Dynamic {

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
