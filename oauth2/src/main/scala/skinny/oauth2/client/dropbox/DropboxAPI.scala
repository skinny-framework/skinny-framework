package skinny.oauth2.client.dropbox

import skinny.logging.LoggerProvider
import skinny.oauth2.client._
import skinny.util.JSONStringOps
import scala.util.control.NonFatal

/**
 * Dropbox API client.
 */
trait DropboxAPI extends LoggerProvider {

  def accountInfo(token: OAuth2Token): Option[DropboxUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://api.dropbox.com/1/account/info").accessToken(token.accessToken)
      }
      logger.debug(s"Dropbox authorized user: ${response.body}")
      JSONStringOps.fromJSONString[RawDropboxUser](response.body).map(_.toDropboxUser)
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Dropbox user information because ${e.getMessage}", e)
        None
    }
  }

}

object DropboxAPI extends DropboxAPI
