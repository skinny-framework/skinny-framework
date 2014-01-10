package skinny.servlet.filter

import javax.servlet._
import javax.servlet.http._

/**
 * Uncaught exception notifier
 */
trait UncaughtExceptionNotifier extends Filter {

  /**
   * Notifies about exception.
   *
   * @param t exception
   * @param request http request
   * @param response http response
   */
  def notify(t: Throwable, request: HttpServletRequest, response: HttpServletResponse): Unit

  // -------------------------------
  // javax.servlet.Filter APIs

  override def init(filterConfig: FilterConfig): Unit = {
  }

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    try chain.doFilter(request, response)
    catch {
      case e: Throwable =>
        notify(e, request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse])
        throw e
    }
  }

  override def destroy(): Unit = {
  }

  // -------------------------------

}
