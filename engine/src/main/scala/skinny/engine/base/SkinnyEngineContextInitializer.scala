package skinny.engine.base

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

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
  implicit def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    SkinnyEngineContext.build(ctx, mainThreadDynamicRequest.value, mainThreadDynamicResponse.value)
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
