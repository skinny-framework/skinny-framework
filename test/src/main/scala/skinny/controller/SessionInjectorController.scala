package skinny.controller

import skinny._

/**
 * Session injector for testing & debugging
 */
private[skinny] object SessionInjectorController extends SessionInjectorController {
  get("/session.json")(get()(Format.JSON))
  put("/session")(update)
}

/**
 * Session injector for testing & debugging.
 */
trait SessionInjectorController extends SkinnyController {

  /**
   * Shows whole session attributes.
   *
   * @param format JSON by default
   * @return session attributes
   */
  def get()(implicit format: Format = Format.JSON) = renderWithFormat(session.toMap)

  /**
   * Injects a value into session.
   *
   * @param format format
   * @return none
   */
  def update()(implicit format: Format = Format.HTML) = {
    if (isProduction) haltWithBody(404)
    else params.foreach {
      case (key, value) =>
        logger.debug(s"${key} -> ${value}")
        session.put(key, value)
    }
  }

}
