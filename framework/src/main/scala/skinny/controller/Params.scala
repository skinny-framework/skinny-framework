package skinny.controller

import scala.language.dynamics

case class Params(underlying: Map[String, Any]) extends Dynamic {

  def selectDynamic(name: String): Option[Any] = underlying.get(name)

}
