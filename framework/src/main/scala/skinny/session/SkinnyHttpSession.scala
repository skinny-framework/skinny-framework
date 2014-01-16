package skinny.session

/**
 * SkinnySession works as a shared session for multiple servers.
 */
trait SkinnyHttpSession {

  def getAttributeOrElseUpdate(name: String, default: Any): Any

  def getAttribute(name: String): Option[Any]

  def setAttribute(name: String, value: Any): Unit

  def removeAttribute(name: String): Unit

  def save(): Unit

  def invalidate(): Unit

}
