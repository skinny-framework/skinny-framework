package skinny.session.servlet

import javax.servlet.http.HttpSession
import skinny.session.SkinnyHttpSession

case class SkinnyHttpSessionWrapper(
    session: HttpSession,
    skinnySession: SkinnyHttpSession
) extends HttpSession {

  /**
    * Just to sync invalidate event to skinny session.
    */
  def invalidate() = {
    session.invalidate()
    skinnySession.invalidate()
  }

  def getCreationTime                        = session.getCreationTime
  def getId                                  = session.getId
  def getLastAccessedTime                    = session.getLastAccessedTime
  def getServletContext                      = session.getServletContext
  def setMaxInactiveInterval(interval: Int)  = session.setMaxInactiveInterval(interval)
  def getMaxInactiveInterval                 = session.getMaxInactiveInterval
  def getSessionContext                      = session.getSessionContext
  def getAttribute(name: String)             = session.getAttribute(name)
  def getValue(name: String)                 = session.getValue(name)
  def getAttributeNames                      = session.getAttributeNames
  def getValueNames                          = session.getValueNames
  def setAttribute(name: String, value: Any) = session.setAttribute(name, value)
  def putValue(name: String, value: Any)     = session.putValue(name, value)
  def removeAttribute(name: String)          = session.removeAttribute(name)
  def removeValue(name: String)              = session.removeValue(name)
  def isNew                                  = session.isNew

}
