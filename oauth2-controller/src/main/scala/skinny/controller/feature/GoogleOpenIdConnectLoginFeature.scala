package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.google._

/**
  * Google OpenId Connect UserInfo based Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_GOOGLE=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE=yyy
  * }}}
  */
trait GoogleOpenIdConnectLoginFeature extends OAuth2LoginFeature[GoogleOpenIdConnectUser] {

  override protected def provider = OAuth2Provider.Google

  override protected def scope = "openid email profile"

  override protected def retrieveAuthorizedUser(token: OAuth2Token): GoogleOpenIdConnectUser = {
    GoogleOpenIdConnectAPI.userinfo(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
