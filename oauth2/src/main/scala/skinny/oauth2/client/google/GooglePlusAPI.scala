package skinny.oauth2.client.google

import skinny.logging.Logging
import skinny.oauth2.client._
import skinny.util.JSONStringOps

import scala.util.control.NonFatal

/**
 * Google Plus API.
 */
trait GooglePlusAPI extends Logging {

  def me(token: OAuth2Token): Option[GoogleUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://www.googleapis.com/plus/v1/people/me").accessToken(token.accessToken)
      }
      logger.debug(s"Google authorized user: ${response.body}")
      JSONStringOps.fromJSONString[GoogleUser](response.body)
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Facebook user information because ${e.getMessage}", e)
        None
    }
  }

}

object GooglePlusAPI extends GooglePlusAPI
