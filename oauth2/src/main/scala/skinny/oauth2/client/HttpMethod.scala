package skinny.oauth2.client

import org.apache.oltu.oauth2.common.OAuth.{ HttpMethod => OltuHttpMethod }

/**
  * HTTP method.
  */
case class HttpMethod(value: String)

object HttpMethod {

  val DELETE = HttpMethod(OltuHttpMethod.DELETE)
  val GET    = HttpMethod(OltuHttpMethod.GET)
  val POST   = HttpMethod(OltuHttpMethod.POST)
  val PUT    = HttpMethod(OltuHttpMethod.PUT)

}
