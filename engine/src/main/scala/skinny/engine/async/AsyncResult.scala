package skinny.engine.async

import scala.language.postfixOps

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.context.SkinnyEngineContext

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object AsyncResult {

  def apply(action: Any)(
    implicit ctx: SkinnyEngineContext, executionContext: ExecutionContext): AsyncResult = {
    withFuture(Future(action))
  }

  def withFuture(future: Future[_])(implicit ctx: SkinnyEngineContext): AsyncResult = {
    new AsyncResult {
      override val is = future
    }
  }

}

abstract class AsyncResult(
    implicit val skinnyEngineContext: SkinnyEngineContext) {

  val request: HttpServletRequest = skinnyEngineContext.surelyStable.request

  val response: HttpServletResponse = skinnyEngineContext.surelyStable.response

  val servletContext: ServletContext = skinnyEngineContext.surelyStable.servletContext

  // This is a Duration instead of a timeout because a duration has the concept of infinity
  // If you need to run long-live operations, override this value
  implicit def timeout: Duration = 10.seconds

  val is: Future[_]

}
