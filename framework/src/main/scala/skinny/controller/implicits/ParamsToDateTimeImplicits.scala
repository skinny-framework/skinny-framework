package skinny.controller.implicits

import scala.language.implicitConversions

import skinny.StrongParameters

/**
 * Implicit conversions for enabling Params acts as factory of strong parameters.
 */
trait ParamsToDateTimeImplicits { self: org.scalatra.ScalatraBase =>

  implicit def convertParamsToStrongParameters(params: org.scalatra.Params): StrongParameters = {
    StrongParameters(params)
  }

}
