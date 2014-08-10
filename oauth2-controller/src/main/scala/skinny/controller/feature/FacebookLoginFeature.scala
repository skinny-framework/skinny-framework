package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.facebook._

/**
 * GitHub OAuth2 Login Controller.
 *
 * {{{
 * export SKINNY_OAUTH2_CLIENT_ID_FACEBOOK=xxx
 * export SKINNY_OAUTH2_CLIENT_SECRET_FACEBOOK=yyy
 * }}}
 */
trait FacebookLoginFeature extends OAuth2LoginFeature[FacebookUser] {

  override protected def provider = OAuth2Provider.Facebook

  override protected def retrieveAuthorizedUser(token: OAuth2Token): FacebookUser = {
    FacebookGraphAPI.me(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
