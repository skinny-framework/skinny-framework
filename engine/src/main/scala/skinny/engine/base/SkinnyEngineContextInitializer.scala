package skinny.engine.base

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.ServletConcurrencyException
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
trait SkinnyEngineContextInitializer { self: ServletContextAccessor =>

  /**
   * The currently scoped request.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicRequest = new DynamicVariable[HttpServletRequest](null)

  /**
   * The currently scoped response.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicResponse = new DynamicVariable[HttpServletResponse](null)

  /**
   * Skinny Engine Context
   */
  def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    if (mainThreadDynamicRequest.value != null) {
      SkinnyEngineContext.build(ctx, mainThreadDynamicRequest.value, mainThreadDynamicResponse.value)
    } else {
      // -------------------------------------------------
      // NOTE: this behavior doesn't always happen
      //
      // dynamic request value is stored only for Servlet main thread.
      // When DSLs that need stable SkinnyEngineContext are accessed inside Future value's #map operation and so on,
      // framework users sometimes specify explicit SkinnyEngineContext.
      //
      // This exception's message shows framework users what they need to do.
      // -------------------------------------------------
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
    mainThreadDynamicRequest.withValue(request) {
      f
    }
  }

  /**
   * Executes the block with the given response bound to the `response`
   * method.
   */
  protected def withResponse[A](response: HttpServletResponse)(f: => A) = {
    mainThreadDynamicResponse.withValue(response) {
      f
    }
  }

}
