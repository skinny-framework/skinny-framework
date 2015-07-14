package org

package object scalatra {

  @deprecated("Use skinny.engine.SkinnyEngineBase instead", since = "2.0.0")
  type ScalatraBase = skinny.engine.SkinnyEngineBase

  @deprecated("Use skinny.engine.SkinnyEngineServlet instead", since = "2.0.0")
  type ScalatraServlet = skinny.engine.SkinnyEngineServlet

  @deprecated("Use skinny.engine.SkinnyEngineFilter instead", since = "2.0.0")
  type ScalatraFilter = skinny.engine.SkinnyEngineFilter

  @deprecated("Use skinny.engine.SkinnyEngineException instead", since = "2.0.0")
  type ScalatraException = skinny.engine.SkinnyEngineException

}
