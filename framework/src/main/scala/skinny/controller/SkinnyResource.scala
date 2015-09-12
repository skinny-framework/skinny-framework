package skinny.controller

import skinny.TypeConverter
import skinny.micro.implicits.TypeConverters

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 * SkinnyResource is surely inspired by Rails ActiveSupport.
 */
trait SkinnyResource extends SkinnyResourceWithId[Long] {

  implicit override val skinnyMicroParamsIdTypeConverter: TypeConverter[String, Long] = TypeConverters.stringToLong
}

trait SkinnyResourceWithId[Id]
  extends SkinnyController
  with SkinnyResourceActions[Id]
  with SkinnyResourceRoutes[Id]
