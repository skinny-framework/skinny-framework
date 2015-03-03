package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.backlog._

/**
 * Typetalk OAuth2 Login Controller.
 *
 * {{{
 * export SKINNY_OAUTH2_CLIENT_ID_BACKLOG=xxx
 * export SKINNY_OAUTH2_CLIENT_SECRET_BACKLOG=yyy
 * }}}
 */
trait BacklogLoginFeature extends OAuth2LoginFeature[BacklogUser] {

  override protected def provider = {
    OAuth2Provider.Backlog.copy(
      authorizationEndpoint = OAuth2Provider.Backlog.authorizationEndpoint.replaceFirst("\\{space\\}", spaceID),
      accessTokenEndpoint = OAuth2Provider.Backlog.accessTokenEndpoint.replaceFirst("\\{space\\}", spaceID)
    )
  }

  def spaceID: String

  override protected def retrieveAuthorizedUser(token: OAuth2Token): BacklogUser = {
    BacklogAPI(spaceID).myself(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}

