package org

import scala.language.implicitConversions

import org.scalatra.servlet.FileUploadSupport
import org.scalatra.util.{ MultiMap, MapWithIndifferentAccess, MultiMapHeadView }

package object scalatra
    extends Control // make halt and pass visible to helpers outside the DSL
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

  val EnvironmentKey = "org.scalatra.environment"

  val MultiParamsKey = "org.scalatra.MultiParams"

  type FuturesAndFlashStack = FutureSupport with FlashMapSupport
  type FuturesAndFlashStackWithCsrf = FuturesAndFlashStack with CsrfTokenSupport
  type FuturesAndFlashStackWithXsrf = FuturesAndFlashStack with XsrfTokenSupport
  type FileUploadStack = FutureSupport with FlashMapSupport with FileUploadSupport

}
