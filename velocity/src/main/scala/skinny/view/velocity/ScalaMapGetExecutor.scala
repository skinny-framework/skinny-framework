package skinny.view.velocity

import org.apache.velocity.runtime.log.Log
import org.apache.velocity.runtime.parser.node.MapGetExecutor
import org.apache.velocity.util.introspection.Introspector

/**
 * Scala friendly Map accessor in Velocity Template
 *
 * @param log Logger
 * @param introspector Velocity Introspector
 * @param clazz target class
 * @param property target property
 */
class ScalaMapGetExecutor(log: Log, introspector: Introspector, clazz: Class[_], property: String)
    extends MapGetExecutor(log, clazz, property) {

  override def isAlive: Boolean = true

  override protected def discover(clazz: Class[_]): Unit = {
    setMethod(introspector.getMethod(clazz, property, Array.empty[AnyRef]))
  }

  override def execute(o: AnyRef): AnyRef = {
    Option(getMethod)
      .map { method => method.invoke(o) }
      .getOrElse { o.asInstanceOf[Map[String, AnyRef]].getOrElse(property, null) }
  }

}
