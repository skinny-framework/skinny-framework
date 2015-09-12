package skinny.controller

import skinny.micro.implicits.{ TypeConverter, TypeConverters }

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 * SkinnyApiResource is surely inspired by Rails ActiveSupport.
 */
trait SkinnyApiResource extends SkinnyApiResourceWithId[Long] {

  implicit override val skinnyMicroParamsIdTypeConverter: TypeConverter[String, Long] = TypeConverters.stringToLong
}

trait SkinnyApiResourceWithId[Id]
  extends SkinnyApiController
  with SkinnyApiResourceActions[Id]
  with SkinnyApiResourceRoutes[Id]
