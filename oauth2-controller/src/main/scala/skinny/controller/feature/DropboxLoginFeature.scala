package skinny.controller.feature

import skinny.oauth2.client._
import skinny.oauth2.client.dropbox._

/**
  * Dropbox OAuth2 Login Controller.
  *
  * {{{
  * export SKINNY_OAUTH2_CLIENT_ID_DROPBOX=xxx
  * export SKINNY_OAUTH2_CLIENT_SECRET_DROPBOX=yyy
  * }}}
  */
trait DropboxLoginFeature extends OAuth2LoginFeature[DropboxUser] {

  override protected def provider = OAuth2Provider.Dropbox

  override protected def retrieveAuthorizedUser(token: OAuth2Token): DropboxUser = {
    DropboxAPI.accountInfo(token).getOrElse {
      handleWhenLoginFailed()
      haltWithBody(401)
    }
  }

}
