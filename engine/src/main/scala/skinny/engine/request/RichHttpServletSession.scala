package skinny.engine.request

import javax.servlet.http.HttpSession

import skinny.engine.data.AttributesMap

/**
 * Extension methods to the standard HttpSession.
 */
case class RichHttpServletSession(session: HttpSession) extends AttributesMap {

  def id: String = session.getId

  protected def attributes: HttpSession = session

}
