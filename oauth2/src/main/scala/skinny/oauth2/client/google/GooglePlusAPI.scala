package skinny.oauth2.client.google

import skinny.logging.LoggerProvider
import skinny.oauth2.client._
import skinny.json.JSONStringOps

import scala.util.control.NonFatal

/**
  * Google Plus API.
  */
trait GooglePlusAPI extends LoggerProvider {

  def me(token: OAuth2Token): Option[GoogleUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://www.googleapis.com/plus/v1/people/me").accessToken(token.accessToken)
      }
      logger.debug(s"Google authorized user: ${response.body}")
      JSONStringOps.fromJSONString[GoogleUser](response.body).toOption
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Google user information because ${e.getMessage}", e)
        None
    }
  }

}

object GooglePlusAPI extends GooglePlusAPI
