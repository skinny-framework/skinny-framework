package skinny.controller

import skinny.TypeConverter
import skinny.micro.implicits.TypeConverters

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 * SkinnyResourceServlet is surely inspired by Rails ActiveSupport.
 */
trait SkinnyResourceServlet extends SkinnyResourceServletWithId[Long] {

  implicit override val skinnyMicroParamsIdTypeConverter: TypeConverter[String, Long] = TypeConverters.stringToLong
}

trait SkinnyResourceServletWithId[Id]
  extends SkinnyServlet
  with SkinnyResourceActions[Id]
  with SkinnyResourceRoutes[Id]

