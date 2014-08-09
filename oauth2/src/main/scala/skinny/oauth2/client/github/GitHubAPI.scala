package skinny.oauth2.client.github

import skinny.logging.Logging
import skinny.oauth2.client._
import skinny.util.JSONStringOps
import scala.util.control.NonFatal

/**
 * GitHub API client.
 */
trait GitHubAPI extends Logging {

  def user(token: OAuth2Token): Option[GitHubUser] = {
    try {
      val response = OAuth2Client.resource {
        BearerRequest("https://api.github.com/user").accessToken(token.accessToken)
      }
      logger.debug(s"GitHub authorized user: ${response.body}")
      JSONStringOps.fromJSONString[GitHubUser](response.body)
    } catch {
      case NonFatal(e) =>
        logger.error(s"Failed to get current GitHub user information because ${e.getMessage}", e)
        None
    }
  }

}

object GitHubAPI extends GitHubAPI
