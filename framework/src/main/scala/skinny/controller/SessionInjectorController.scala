package skinny.controller

import skinny._

object SessionInjectorController extends SessionInjectorController {
  get("/session.json")(get()(Format.JSON))
  put("/session")(update)
}

/**
 * Session injector for testing & debugging
 */
class SessionInjectorController extends SkinnyController {

  def get()(implicit format: Format = Format.JSON) = renderWithFormat(session.toMap)

  def update()(implicit format: Format = Format.HTML) = if (isProduction) {
    haltWithBody(404)
  } else {
    params.foreach {
      case (key, value) =>
        logger.debug(s"${key} -> ${value}")
        session(key) = value
    }
  }

}
