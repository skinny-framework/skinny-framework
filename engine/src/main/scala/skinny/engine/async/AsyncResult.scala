package skinny.engine.async

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.context.SkinnyEngineContext

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

abstract class AsyncResult(
  implicit override val skinnyEngineContext: SkinnyEngineContext)
    extends SkinnyEngineContext {

  implicit val request: HttpServletRequest = skinnyEngineContext.request

  implicit val response: HttpServletResponse = skinnyEngineContext.response

  val servletContext: ServletContext = skinnyEngineContext.servletContext

  // This is a Duration instead of a timeout because a duration has the concept of infinity
  implicit def timeout: Duration = 30 seconds

  val is: Future[_]

}
