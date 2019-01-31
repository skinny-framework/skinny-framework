package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.google._

/**
  * Google OAuth2 Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_GOOGLE=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE=yyy
  * }}}
  */
@deprecated("Google+ API will be shutdown on March 7, 2019. https://developers.google.com/+/api-shutdown")
trait GoogleLoginFeature extends OAuth2LoginFeature[GoogleUser] {

  override protected def provider = OAuth2Provider.Google

  override protected def scope = "openid email"

  override protected def retrieveAuthorizedUser(token: OAuth2Token): GoogleUser = {
    GooglePlusAPI.me(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
