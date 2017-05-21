package skinny.controller.feature

import skinny.filter.SkinnySessionFilter
import skinny.oauth2.client._

/**
  * SkinnySession wired OAuth2LoginFeature.
  */
trait SkinnySessionOAuth2LoginFeature[U <: OAuth2User] { self: OAuth2LoginFeature[U] with SkinnySessionFilter =>

  override protected def state: String = skinnySession.getAs[String](sessionOAuth2StateName).getOrElse {
    val state: String = generateStateValue()
    skinnySession.setAttribute(sessionOAuth2StateName, state)
    state
  }

}
