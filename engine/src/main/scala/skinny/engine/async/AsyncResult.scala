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

  val request: HttpServletRequest = skinnyEngineContext.readOnlyRequest

  val response: HttpServletResponse = skinnyEngineContext.response

  val servletContext: ServletContext = skinnyEngineContext.servletContext

  // This is a Duration instead of a timeout because a duration has the concept of infinity
  implicit def timeout: Duration = 30 seconds

  val is: Future[_]

}
