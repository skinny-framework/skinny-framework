package skinny.engine.async

import scala.language.postfixOps

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.context.SkinnyEngineContext

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

/**
 * Result value of async skinny-engine.
 */
abstract class AsyncResult(
    implicit val context: SkinnyEngineContext) {

  val request: HttpServletRequest = {
    context.surelyStable(context.unstableAccessValidation).request
  }

  val response: HttpServletResponse = {
    context.surelyStable(context.unstableAccessValidation).response
  }

  val servletContext: ServletContext = {
    context.surelyStable(context.unstableAccessValidation).servletContext
  }

  // This is a Duration instead of a timeout because a duration has the concept of infinity
  // If you need to run long-live operations, override this value
  implicit def timeout: Duration = 10.seconds

  val is: Future[_]

}

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