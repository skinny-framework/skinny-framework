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
trait GoogleLoginFeature extends OAuth2LoginFeature[GoogleUser] {

  override protected def provider = OAuth2Provider.Google

  override protected def createAuthenticationRequest(): AuthenticationRequest = {
    val req = AuthenticationRequest(provider)
      .clientId(clientId)
      .responseType(ResponseType.Code)
      .state(state)
      .scope("openid email")
      .redirectURI(redirectURI)
    if (scope != null) req.scope(scope) else req
  }

  override protected def retrieveAuthorizedUser(token: OAuth2Token): GoogleUser = {
    GooglePlusAPI.me(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
