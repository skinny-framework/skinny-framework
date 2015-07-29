package skinny.engine.base

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.{ UnstableAccessValidation, ServletConcurrencyException }
import skinny.engine.context.SkinnyEngineContext

import scala.util.DynamicVariable

/**
 * The SkinnyEngine DSL requires a dynamically scoped request and response to initialize SkinnyEngineContext.
 *
 * This is formerly known as Scalatra's dynamic scope.
 * This trick is explained in greater detail in Gabriele Renzi's blog
 * post about Step, out of which SkinnyEngine grew:
 * http://www.riffraff.info/2009/4/11/step-a-scala-web-picoframework
 */
trait SkinnyEngineContextInitializer {

  self: ServletContextAccessor with UnstableAccessValidationConfig =>

  private[this] case class HttpServletRequestHolder(
    value: HttpServletRequest,
    threadId: Long)

  private[this] case class HttpServletResponseHolder(
    value: HttpServletResponse,
    threadId: Long)

  private[this] def currentThreadId: Long = Thread.currentThread.getId

  /**
   * The currently scoped request.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicRequest: DynamicVariable[Option[HttpServletRequestHolder]] = {
    new DynamicVariable[Option[HttpServletRequestHolder]](None)
  }

  /**
   * The currently scoped response.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicResponse: DynamicVariable[Option[HttpServletResponseHolder]] = {
    new DynamicVariable[Option[HttpServletResponseHolder]](None)
  }

  /**
   * Skinny Engine Context
   */
  def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    (mainThreadDynamicRequest.value, mainThreadDynamicResponse.value) match {
      case (Some(req), _) if (req.threadId != currentThreadId) =>
        // dynamic variable access from another thread detected
        throw new ServletConcurrencyException
      case (Some(req), Some(resp)) =>
        SkinnyEngineContext.build(
          ctx,
          req.value,
          resp.value,
          UnstableAccessValidation(unstableAccessValidationEnabled)
        )
      case _ =>
        // If the dynamic variables are None, this code is running on another thread
        throw new ServletConcurrencyException
    }
  }

  def context: SkinnyEngineContext = skinnyEngineContext(servletContext)

  def request(implicit ctx: SkinnyEngineContext = context): HttpServletRequest = ctx.request

  def response(implicit ctx: SkinnyEngineContext = context): HttpServletResponse = ctx.response

  protected def withRequestResponse[A](request: HttpServletRequest, response: HttpServletResponse)(f: => A) = {
    withRequest(request) {
      withResponse(response) {
        f
      }
    }
  }

  /**
   * Executes the block with the given request bound to the `request`
   * method.
   */
  protected def withRequest[A](request: HttpServletRequest)(f: => A): A = {
    mainThreadDynamicRequest.withValue(
      Some(HttpServletRequestHolder(request, currentThreadId))) {
        f
      }
  }

  /**
   * Executes the block with the given response bound to the `response`
   * method.
   */
  protected def withResponse[A](response: HttpServletResponse)(f: => A) = {
    mainThreadDynamicResponse.withValue(
      Some(HttpServletResponseHolder(response, currentThreadId))) {
        f
      }
  }

}
