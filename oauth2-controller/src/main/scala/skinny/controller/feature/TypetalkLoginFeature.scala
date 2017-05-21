package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.typetalk.{ TypetalkAPI, TypetalkUser }

/**
  * Typetalk OAuth2 Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_TYPETALK=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_TYPETALK=yyy
  * }}}
  */
trait TypetalkLoginFeature extends OAuth2LoginFeature[TypetalkUser] {

  override protected def provider = OAuth2Provider.Typetalk

  override protected def scope = "my"

  override protected def retrieveAuthorizedUser(token: OAuth2Token): TypetalkUser = {
    TypetalkAPI.profile(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
