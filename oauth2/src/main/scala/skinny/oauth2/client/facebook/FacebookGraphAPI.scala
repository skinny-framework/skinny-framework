package skinny.oauth2.client.facebook

import skinny.oauth2.client._
import skinny.logging.Logging
import skinny.util.JSONStringOps
import scala.util.control.NonFatal

/**
 * Facebook Graph API client.
 */
trait FacebookGraphAPI extends Logging {

  def me(token: OAuth2Token): Option[FacebookUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://graph.facebook.com/v2.1/me").accessToken(token.accessToken)
      }
      logger.debug(s"Facebook authorized user: ${response.body}")
      JSONStringOps.fromJSONString[FacebookUser](response.body)
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Facebook user information because ${e.getMessage}", e)
        None
    }
  }

}

object FacebookGraphAPI extends FacebookGraphAPI
