package skinny.controller.implicits

import scala.language.implicitConversions

import org.scalatra._
import skinny.orm._

trait ParamsPermitImplicits { self: ScalatraBase =>

  implicit def convertParamsToStrongParameters(params: Params): StrongParameters = {
    StrongParameters(params)
  }

}
