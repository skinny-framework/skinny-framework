package skinny.session

import javax.servlet.http.HttpSession
import skinny.logging.LoggerProvider
import skinny.session.jdbc.SkinnySession

case class SkinnyHttpSessionJDBCImpl(underlying: HttpSession, skinnySession: SkinnySession)
    extends SkinnyHttpSession
    with LoggerProvider {

  override def save() = skinnySession.save()

  override def getAttributeOrElseUpdate(name: String, default: Any) = getAttribute(name).getOrElse {
    setAttribute(name, default)
    default
  }

  override def getAttribute(name: String): Option[Any] = Option(skinnySession.getAttribute(name))

  override def setAttribute(name: String, value: Any): Unit = skinnySession.setAttribute(name, value)

  override def removeAttribute(name: String): Unit = skinnySession.removeAttribute(name)

  override def invalidate(): Unit = SkinnySession.invalidate(underlying.getId)

}
