package skinny.controller

import java.io._
import java.util.Base64

import skinny.micro.Format
import skinny.util.LoanPattern._
import skinny.logging.LoggerProvider

import scala.util.control.NonFatal

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
trait SessionInjectorController extends SkinnyApiController with LoggerProvider {

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
    else
      params.foreach {
        case (key, value) => session.put(key, deserialize(value))
      }
  }

  /**
    * Serialize an object to string.
    *
    * @param obj object
    * @tparam A type of object
    * @return serialized string
    */
  def serialize[A](obj: A): String = {
    val bao = new ByteArrayOutputStream
    using(new ObjectOutputStream(bao)) {
      _.writeObject(obj)
    }
    Base64.getEncoder.encodeToString(bao.toByteArray)
  }

  /**
    * Deserialize an object from string
    *
    * @param str string
    * @return object
    */
  def deserialize(str: String): AnyRef = {
    try {
      val bytes = Base64.getDecoder.decode(str)
      val bai   = new ByteArrayInputStream(bytes)
      using(new ObjectInputStream(bai)) {
        _.readObject
      }
    } catch {
      case NonFatal(e) => {
        logger.error(s"Failed to deserialize the value because ${e.getMessage}", e)
        null
      }
    }
  }

}
