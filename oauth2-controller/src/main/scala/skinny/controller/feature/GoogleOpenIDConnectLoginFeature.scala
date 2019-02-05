package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.google._

/**
  * Google OpenID Connect UserInfo based Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_GOOGLE=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE=yyy
  * }}}
  */
trait GoogleOpenIDConnectLoginFeature extends OAuth2LoginFeature[GoogleOpenIDConnectUser] {

  override protected def provider = OAuth2Provider.Google

  override protected def scope = "openid email profile"

  override protected def retrieveAuthorizedUser(token: OAuth2Token): GoogleOpenIDConnectUser = {
    GoogleOpenIDConnectAPI.userinfo(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
