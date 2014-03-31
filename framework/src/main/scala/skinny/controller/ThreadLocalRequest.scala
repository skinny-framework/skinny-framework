package skinny.controller

import scala.util.DynamicVariable
import javax.servlet.http.HttpServletRequest

/**
 * Request holder as a thread-local variable.
 */
object ThreadLocalRequest {

  private[this] val _req = new DynamicVariable[HttpServletRequest](null)

  def save(req: => HttpServletRequest): HttpServletRequest = {
    _req.value = req
    _req.value
  }

  def get(): HttpServletRequest = _req.value

}
