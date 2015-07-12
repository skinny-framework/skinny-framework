package skinny.engine.constant

sealed trait Scheme

case object Http extends Scheme

case object Https extends Scheme

