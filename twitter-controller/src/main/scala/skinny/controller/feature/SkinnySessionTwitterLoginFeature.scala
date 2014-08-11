package skinny.controller.feature

import skinny.filter.SkinnySessionFilter
import twitter4j.auth.{AccessToken, RequestToken}

/**
 * SkinnySession wired TwitterLoginFeature.
 */
trait SkinnySessionTwitterLoginFeature extends TwitterLoginFeature { self: SkinnySessionFilter =>

  // ----------------------------------------
  // Request Token

  override protected def saveRequestToken(requestToken: RequestToken): Unit = {
    skinnySession.setAttribute(sessionRequestTokenName, requestToken.getToken)
    skinnySession.setAttribute(sessionRequestTokenSecretName, requestToken.getTokenSecret)
  }

  override protected def currentRequestToken(): Option[RequestToken] = {
    for {
      token <- skinnySession.getAttribute(sessionRequestTokenName).map(_.toString)
      tokenSecret <- skinnySession.getAttribute(sessionRequestTokenSecretName).map(_.toString)
    } yield {
      new RequestToken(token, tokenSecret)
    }
  }

  override protected def deleteSavedRequestToken(): Unit = {
    skinnySession.removeAttribute(sessionRequestTokenName)
    skinnySession.removeAttribute(sessionRequestTokenSecretName)
  }

  // ----------------------------------------
  // Access Token

  override protected def saveAccessToken(accessToken: AccessToken): Unit = {
    skinnySession.setAttribute(sessionAccessTokenName, accessToken.getToken)
    skinnySession.setAttribute(sessionAccessTokenSecretName, accessToken.getTokenSecret)
  }

  override protected def currentAccessToken(): Option[AccessToken] = {
    for {
      token <- skinnySession.getAttribute(sessionAccessTokenName).map(_.toString)
      tokenSecret <- skinnySession.getAttribute(sessionAccessTokenSecretName).map(_.toString)
    } yield {
      new AccessToken(token, tokenSecret)
    }
  }

  override protected def deleteSavedAccessToken(): Unit = {
    skinnySession.removeAttribute(sessionAccessTokenName)
    skinnySession.removeAttribute(sessionAccessTokenSecretName)
  }

}
