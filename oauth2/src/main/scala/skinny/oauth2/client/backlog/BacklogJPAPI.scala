package skinny.oauth2.client.backlog

import skinny.logging.LoggerProvider
import skinny.oauth2.client.{ BearerRequest, OAuth2Client, OAuth2Token }
import skinny.json.JSONStringOps

import scala.util.control.NonFatal

case class BacklogJPAPI(spaceID: String) extends LoggerProvider {

  def myself(token: OAuth2Token): Option[BacklogUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest(s"https://${spaceID}.backlog.jp/api/v2/users/myself").accessToken(token.accessToken)
      }
      logger.debug(s"Backlog authorized user: ${response.body}")
      JSONStringOps.fromJSONString[BacklogUser](response.body).toOption
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current Backlog user information because ${e.getMessage}", e)
        None
    }
  }

}
