package skinny.controller

import org.scalatra.util.conversion.{ Conversions, TypeConverter }

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 * SkinnyResource is surely inspired by Rails ActiveSupport.
 */
trait SkinnyApiResource extends SkinnyApiResourceWithId[Long] {

  implicit override val scalatraParamsIdTypeConverter: TypeConverter[String, Long] = Conversions.stringToLong
}

trait SkinnyApiResourceWithId[Id]
  extends SkinnyApiController
  with SkinnyApiResourceActions[Id]
  with SkinnyApiResourceRoutes[Id]
