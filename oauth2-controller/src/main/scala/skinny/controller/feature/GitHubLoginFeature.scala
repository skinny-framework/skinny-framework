package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.github._

/**
  * GitHub OAuth2 Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_GITHUB=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_GITHUB=yyy
  * }}}
  */
trait GitHubLoginFeature extends OAuth2LoginFeature[GitHubUser] {

  override protected def provider = OAuth2Provider.GitHub

  override protected def retrieveAuthorizedUser(token: OAuth2Token): GitHubUser = {
    GitHubAPI.user(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
