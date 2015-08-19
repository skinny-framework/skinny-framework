package skinny.engine.implicits

import scala.language.implicitConversions

/**
 * Lowest priority implicit conversions for TypeConverters.
 */
trait LowestPriorityImplicits extends TypeConverterSupport {

  implicit def lowestPriorityAny2T[T: Manifest]: TypeConverter[Any, T] = safe {
    case a if manifest[T].runtimeClass.isAssignableFrom(a.getClass) => a.asInstanceOf[T]
  }

}
