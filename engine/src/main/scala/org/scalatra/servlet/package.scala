package org.scalatra

package object servlet {

  @deprecated("Use skinny.engine.SkinnyEngineListener instead", since = "2.0.0")
  type ScalatraListener = skinny.engine.SkinnyEngineListener

  @deprecated("Use skinny.engine.SkinnyEngineBase instead", since = "2.0.0")
  type ServletBase = skinny.engine.SkinnyEngineBase

}
