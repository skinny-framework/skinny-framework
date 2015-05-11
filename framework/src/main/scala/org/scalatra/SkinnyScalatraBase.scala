package org.scalatra

import org.scalatra.ScalatraBase._
import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.Filter
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest, HttpServlet }
import scala.util.Failure
import skinny.SkinnyEnv
import skinny.logging.Logging

/**
 * Partially patched ScalatraBase for Skinny Framework.
 *
 * Scalatra runs only the first one of after filters. The others are skipped due to
 * the servlet/filter is already marked as "org.scalatra.ScalatraFilter.afterFilters.Run".
 * This means that when several Scalatra filters is already definied below at ScalatraBootstrap.scala,
 * current ScalatraFilter's after callbacks would be ignored (even though all the before callbacks are called).
 *
 * So We've patched ScalatraBase to ignore "org.scalatra.ScalatraFilter.afterFilters.Run" only for Filters.
 * Hope Scalatra to support ignoring "org.scalatra.ScalatraFilter.afterFilters.Run" option to 3rd party.
 */
trait SkinnyScalatraBase extends ScalatraBase with Logging {

  override protected def executeRoutes() {
    var result: Any = null
    var rendered = true

    def runActions = {
      val prehandleException = request.get(PrehandleExceptionKey)
      if (prehandleException.isEmpty) {
        val (rq, rs) = (request, response)
        onCompleted { _ =>
          withRequestResponse(rq, rs) {
            val className = this.getClass.toString
            this match {
              // **** PATCHED ****
              case f: Filter if !rq.contains(s"org.scalatra.ScalatraFilter.afterFilters.Run (${className})") =>
                rq(s"org.scalatra.ScalatraFilter.afterFilters.Run (${className})") = new {}
                runFilters(routes.afterFilters)
              case f: HttpServlet if !rq.contains("org.scalatra.ScalatraServlet.afterFilters.Run") =>
                rq("org.scalatra.ScalatraServlet.afterFilters.Run") = new {}
                runFilters(routes.afterFilters)
              case _ =>
            }
          }
        }
        runFilters(routes.beforeFilters)
        val actionResult = runRoutes(routes(request.requestMethod)).headOption
        // Give the status code handler a chance to override the actionResult
        val r = handleStatusCode(status) getOrElse {
          actionResult orElse matchOtherMethods() getOrElse doNotFound()
        }
        rendered = false
        r
      } else {
        throw prehandleException.get.asInstanceOf[Exception]
      }
    }

    cradleHalt(result = runActions, e => {
      cradleHalt({
        result = errorHandler(e)
        rendered = false
      }, e => {
        runCallbacks(Failure(e))
        try {
          renderUncaughtException(e)
        } finally {
          runRenderCallbacks(Failure(e))
        }
      })
    })

    if (!rendered) renderResponse(result)
  }

  // involuntarily copied to call internal functions

  private[this] def cradleHalt(body: => Any, error: Throwable => Any) = {
    try { body } catch {
      case e: HaltException => renderHaltException(e)
      case e: Throwable => error(e)
    }
  }

  private[this] def matchOtherMethods(): Option[Any] = {
    val allow = routes.matchingMethodsExcept(request.requestMethod, requestPath)
    if (allow.isEmpty) None else liftAction(() => doMethodNotAllowed(allow))
  }

  private[this] def handleStatusCode(status: Int): Option[Any] =
    for {
      handler <- routes(status)
      matchedHandler <- handler(requestPath)
      handlerResult <- invoke(matchedHandler)
    } yield handlerResult

  private def liftAction(action: Action): Option[Any] =
    try {
      Some(action())
    } catch {
      case e: PassException => None
    }

  // TODO fixed?
  override def url(
    path: String,
    params: Iterable[(String, Any)] = Iterable.empty,
    includeContextPath: Boolean = true,
    includeServletPath: Boolean = true,
    absolutize: Boolean = true,
    withSessionId: Boolean = true)(
      implicit request: HttpServletRequest, response: HttpServletResponse): String = {

    try {
      super.url(path, params, includeContextPath, includeServletPath, absolutize)
    } catch {
      case e: NullPointerException =>
        // work around for Scalatra issue
        if (SkinnyEnv.isTest()) "[work around] see https://github.com/scalatra/scalatra/issues/368"
        else throw e
    }
  }

  /**
   * Count execution of error filter registration.
   */
  private[this] lazy val errorMethodCallCountAtSkinnyScalatraBase: AtomicInteger = new AtomicInteger(0)

  /**
   * Detects error filter leak issue as an error.
   */
  protected def detectTooManyErrorFilterRegistrationnAsAnErrorAtSkinnyScalatraBase: Boolean = false

  // https://github.com/scalatra/scalatra/blob/v2.3.1/core/src/main/scala/org/scalatra/ScalatraBase.scala#L333-L335
  override def error(handler: ErrorHandler) {
    val count = errorMethodCallCountAtSkinnyScalatraBase.incrementAndGet()
    if (count > 500) {
      val message = s"skinny's error filter registration for this controller has been evaluated $count times, this behavior will cause memory leak."
      if (detectTooManyErrorFilterRegistrationnAsAnErrorAtSkinnyScalatraBase) throw new RuntimeException(message)
      else logger.warn(message)
    }
    super.error(handler)
  }

}
