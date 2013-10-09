package skinny.controller

import scala.language.dynamics

case class Flash(underlying: org.scalatra.FlashMap) extends Dynamic {

  def get(key: String): Option[Any] = underlying.get(key)

  def selectDynamic(name: String): Option[Any] = underlying.get(name).map { v =>
    v match {
      case Some(v) => v
      case None => null
      case v => v
    }
  }

}

