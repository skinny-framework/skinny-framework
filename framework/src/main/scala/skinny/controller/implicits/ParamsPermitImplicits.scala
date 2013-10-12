package skinny.controller.implicits

import scala.language.implicitConversions

import org.scalatra._
import skinny.orm._

/**
 * Implicit conversions for enabling Params acts as factory of strong parameters.
 */
trait ParamsPermitImplicits { self: ScalatraBase =>

  implicit def convertParamsToStrongParameters(params: Params): StrongParameters = {
    StrongParameters(params)
  }

}
