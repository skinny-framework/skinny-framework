package skinny.view.velocity

import org.apache.velocity.runtime.log.Log
import org.apache.velocity.runtime.parser.node.PropertyExecutor
import org.apache.velocity.util.introspection.Introspector

/**
 * Scala friendly Property accessor for Velocity Template
 * @param log Logger
 * @param introspector Velocity Introspector
 * @param clazz target class
 * @param property target property
 */
class ScalaPropertyExecutor(
  log: Log, introspector: Introspector, clazz: Class[_], property: String
)
    extends PropertyExecutor(log, introspector, clazz, property) {

  override protected def discover(clazz: Class[_], property: String): Unit = {
    setMethod(introspector.getMethod(clazz, property, Array.empty[AnyRef]))
    if (!isAlive) super.discover(clazz, property)
  }

}
