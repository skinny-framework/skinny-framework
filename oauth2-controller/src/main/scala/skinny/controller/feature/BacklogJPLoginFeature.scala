package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.backlog._

/**
  * Typetalk OAuth2 Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_BACKLOG_JP=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_BACKLOG_JP=yyy
  * }}}
  */
trait BacklogJPLoginFeature extends OAuth2LoginFeature[BacklogUser] {

  override protected def provider = {
    OAuth2Provider.BacklogJP.copy(
      authorizationEndpoint = OAuth2Provider.BacklogJP.authorizationEndpoint.replaceFirst("\\{space\\}", spaceID),
      accessTokenEndpoint = OAuth2Provider.BacklogJP.accessTokenEndpoint.replaceFirst("\\{space\\}", spaceID)
    )
  }

  def spaceID: String

  override protected def retrieveAuthorizedUser(token: OAuth2Token): BacklogUser = {
    BacklogJPAPI(spaceID).myself(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
