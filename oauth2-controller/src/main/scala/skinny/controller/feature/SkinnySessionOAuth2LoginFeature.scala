package skinny.controller.feature

import skinny.filter.SkinnySessionFilter
import skinny.oauth2.client._

trait SkinnySessionOAuth2LoginFeature[U <: OAuth2User] { self: OAuth2LoginFeature[U] with SkinnySessionFilter =>

  override protected def state: String = skinnySession.getAs[String](OAuth2LoginFeature.SESSION_OAUTH2_STATE_NAME).getOrElse {
    val state: String = generateStateValue()
    skinnySession.setAttribute(OAuth2LoginFeature.SESSION_OAUTH2_STATE_NAME, state)
    state
  }

}
