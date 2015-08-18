package skinny.oauth2.client.typetalk

import skinny.logging.LoggerProvider
import skinny.oauth2.client.{ BearerRequest, OAuth2Client, OAuth2Token }
import skinny.json.JSONStringOps

import scala.util.control.NonFatal

trait TypetalkAPI extends LoggerProvider {

  def profile(token: OAuth2Token): Option[TypetalkUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://typetalk.in/api/v1/profile").accessToken(token.accessToken)
      }
      logger.debug(s"Typetalk authorized user: ${response.body}")
      JSONStringOps.fromJSONString[MyProfileResponse](response.body).map(_.account)
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Typetalk user information because ${e.getMessage}", e)
        None
    }
  }

}

object TypetalkAPI extends TypetalkAPI

