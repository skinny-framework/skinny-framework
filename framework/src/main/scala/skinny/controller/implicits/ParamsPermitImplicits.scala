package skinny.controller.implicits

import scala.language.implicitConversions

import skinny.micro.{ Params, SkinnyMicroBase }
import skinny.StrongParameters

/**
  * Implicit conversions for enabling Params acts as factory of strong parameters.
  */
trait ParamsPermitImplicits { self: SkinnyMicroBase =>

  implicit def convertParamsToStrongParameters(params: Params): StrongParameters = {
    StrongParameters(params)
  }

}
