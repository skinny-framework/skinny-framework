package skinny.controller

import scala.language.dynamics

/**
  * Scalatra's multi params wrapper.
  *
  * @param underlying Scalatra's params
  */
case class MultiParams(underlying: Map[String, Seq[String]]) extends Dynamic {

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
  def selectDynamic(key: String): Seq[String] = underlying.get(key).getOrElse(Nil)

}
