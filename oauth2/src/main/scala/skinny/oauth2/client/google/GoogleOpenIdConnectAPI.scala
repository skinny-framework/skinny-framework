package skinny.oauth2.client.google

import skinny.logging.LoggerProvider
import skinny.oauth2.client._
import skinny.json.JSONStringOps

import scala.util.control.NonFatal

/**
  * Google OpenID Connect API.
  */
trait GoogleOpenIDConnectAPI extends LoggerProvider {

  def userinfo(token: OAuth2Token): Option[GoogleOpenIDConnectUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://openidconnect.googleapis.com/v1/userinfo").accessToken(token.accessToken)
      }
      logger.debug(s"Google OpenID Connect authorized user: ${response.body}")
      JSONStringOps.fromJSONString[GoogleOpenIDConnectUser](response.body).toOption
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Google OpenID Connect user information because ${e.getMessage}", e)
        None
    }
  }

}

object GoogleOpenIDConnectAPI extends GoogleOpenIDConnectAPI
