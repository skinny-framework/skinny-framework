package skinny

import skinny.engine.base.FlashMapSupport
import skinny.engine.control.HaltPassControl
import skinny.engine.csrf.{ XsrfTokenSupport, CsrfTokenSupport }
import skinny.engine.data.{ MapWithIndifferentAccess, MultiMapHeadView, MultiMap }
import skinny.engine.multipart.FileUploadSupport
import skinny.engine.routing.Route

import scala.language.implicitConversions

package object engine
    extends HaltPassControl // make halt and pass visible to helpers outside the DSL
    //  with DefaultValues // make defaults visible
    {

  object RouteTransformer {

    implicit def fn2transformer(fn: Route => Route): RouteTransformer = new RouteTransformer {
      override def apply(route: Route): Route = fn(route)
    }
  }

  trait RouteTransformer {
    def apply(route: Route): Route
  }

  type MultiParams = MultiMap

  type Params = MultiMapHeadView[String, String] with MapWithIndifferentAccess[String]

  type Action = () => Any

  type ErrorHandler = PartialFunction[Throwable, Any]

  type ContentTypeInferrer = PartialFunction[Any, String]

  type RenderPipeline = PartialFunction[Any, Any]

  val EnvironmentKey = "skinny.engine.environment"

  val MultiParamsKey = "skinny.engine.MultiParams"

}
