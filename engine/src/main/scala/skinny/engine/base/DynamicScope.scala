package skinny.engine.base

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.SkinnyEngineBase
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.request.StableHttpServletRequest

import scala.util.DynamicVariable

/**
 * The SkinnyEngine DSL requires a dynamically scoped request and response.
 * This trick is explained in greater detail in Gabriele Renzi's blog
 * post about Step, out of which SkinnyEngine grew:
 *
 * http://www.riffraff.info/2009/4/11/step-a-scala-web-picoframework
 */
trait DynamicScope { self: ServletContextAccessor =>

  /**
   * The currently scoped request.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicRequest = new DynamicVariable[HttpServletRequest](null)
  private[this] val dynamicReadOnlyRequest = new DynamicVariable[StableHttpServletRequest](null)

  implicit def mainThreadRequest: HttpServletRequest = mainThreadDynamicRequest.value

  // NOTICE: comment out this method will help you when debugging dynamic scope issue
  def request: HttpServletRequest = mainThreadRequest

  /**
   * The currently scoped response.  Valid only inside the `handle` method.
   */
  private[this] val mainThreadDynamicResponse = new DynamicVariable[HttpServletResponse](null)

  implicit def mainThreadResponse: HttpServletResponse = mainThreadDynamicResponse.value

  // NOTICE: comment out this method will help you when debugging dynamic scope issue
  def response: HttpServletResponse = mainThreadResponse

  protected def withRequestResponse[A](request: HttpServletRequest, response: HttpServletResponse)(f: => A) = {
    withRequest(request) {
      withResponse(response) {
        f
      }
    }
  }

  /**
   * Skinny Engine Context
   */
  implicit def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    SkinnyEngineContext.build()(ctx, dynamicReadOnlyRequest.value, mainThreadResponse)
  }

  def context: SkinnyEngineContext = skinnyEngineContext(servletContext)

  /**
   * Executes the block with the given request bound to the `request`
   * method.
   */
  protected def withRequest[A](request: HttpServletRequest)(f: => A): A = {
    dynamicReadOnlyRequest.value = StableHttpServletRequest(request)
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
