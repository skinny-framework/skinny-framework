package skinny.controller

import skinny.engine.implicits.{ TypeConverter, TypeConverters }

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 * SkinnyApiResourceServlet is surely inspired by Rails ActiveSupport.
 */
trait SkinnyApiResourceServlet extends SkinnyApiResourceServletWithId[Long] {

  implicit override val scalatraParamsIdTypeConverter: TypeConverter[String, Long] = TypeConverters.stringToLong
}

trait SkinnyApiResourceServletWithId[Id]
  extends SkinnyApiServlet
  with SkinnyApiResourceActions[Id]
  with SkinnyApiResourceRoutes[Id]
