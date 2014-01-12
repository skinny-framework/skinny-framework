package skinny.session

import javax.servlet.http.HttpSession
import grizzled.slf4j.Logging
import skinny.session.jdbc.SkinnySession

trait SkinnyHttpSession {
  def getAttributeOrElseUpdate(name: String, default: Any): Any
  def getAttribute(name: String): Option[Any]
  def setAttribute(name: String, value: Any): Unit
  def removeAttribute(name: String): Unit
  def save(): Unit
  def invalidate(): Unit
}

class SkinnyHttpSessionJDBCImpl(underlying: HttpSession, skinnySession: SkinnySession) extends SkinnyHttpSession with Logging {

  override def save() = {
    val attributes = skinnySession.attributeNames.map(name => s"$name -> ${skinnySession.getAttribute(name)}")
    logger.debug(s"[Skinny Session] attributes to save: ${attributes}}")
    skinnySession.save()
  }
  override def getAttributeOrElseUpdate(name: String, default: Any) = Option(getAttribute(name)).getOrElse {
    setAttribute(name, default)
    default
  }
  override def getAttribute(name: String): Option[Any] = Option(skinnySession.getAttribute(name))
  override def setAttribute(name: String, value: Any): Unit = skinnySession.setAttribute(name, value)
  override def removeAttribute(name: String): Unit = skinnySession.removeAttribute(name)
  override def invalidate(): Unit = SkinnySession.invalidate(underlying.getId)

}
