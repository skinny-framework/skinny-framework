package skinny.controller

import scala.language.dynamics

/**
 * [[org.scalatra.FlashMap]] wrapper.
 *
 * @param underlying scalatra's FlashMap
 */
case class Flash(underlying: org.scalatra.FlashMap) extends Dynamic {

  /**
   * Returns value if exists.
   *
   * @param key key
   * @return value if exists
   */
  def get(key: String): Option[Any] = underlying.get(key)

  /**
   * Enables accessing key using type-dynamic. Both of the following code is same.
   *
   * {{{
   *   flash.get("notice")
   *   flash.notice
   * }}}
   *
   * @param key key
   * @return value if exists
   */
  def selectDynamic(key: String): Option[Any] = underlying.get(key).map { v =>
    v match {
      case Some(v) => v
      case None => null
      case v => v
    }
  }

}

