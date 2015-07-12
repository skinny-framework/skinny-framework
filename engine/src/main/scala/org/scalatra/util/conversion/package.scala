package org.scalatra.util

package object conversion {

  @deprecated("Use skinny.engine.implicits.TypeConverter instead", since = "2.0.0")
  type TypeConverter[S, T] = skinny.engine.implicits.TypeConverter[S, T]

}
