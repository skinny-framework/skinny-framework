package skinny.engine.data

import scala.collection.mutable.Map

/**
 * @see MapWithIndifferentAccess
 */
trait MutableMapWithIndifferentAccess[B]
    extends MapWithIndifferentAccess[B]
    with Map[String, B] {

  def update(key: Symbol, value: B): Unit = {
    update(key.name, value)
  }

}
