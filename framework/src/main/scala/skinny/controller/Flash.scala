package skinny.controller

import scala.language.dynamics

import skinny.micro.contrib.flash.FlashMap

/**
 * org.scalatra.FlashMap wrapper.
 *
 * @param underlying scalatra's FlashMap
 */
case class Flash(underlying: FlashMap) extends Dynamic {

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
  def selectDynamic(key: String): Option[Any] = get(key)

}

