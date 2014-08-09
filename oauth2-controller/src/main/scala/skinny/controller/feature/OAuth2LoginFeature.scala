package skinny.controller.feature

import java.util.Locale

import org.apache.oltu.oauth2.common.OAuth
import skinny.controller.SkinnyControllerBase
import skinny.oauth2.client._

object OAuth2LoginFeature {

  val DEFAULT_CLIENT_ID_ENV_NAME_PREFIX = "SKINNY_OAUTH2_CLIENT_ID"

  val DEFAULT_CLIENT_SECRET_ENV_NAME_PREFIX = "SKINNY_OAUTH2_CLIENT_SECRET"

  val SESSION_OAUTH2_STATE_NAME = "SKINNY_OAUTH2_STATE"

}

trait OAuth2LoginFeature[U <: OAuth2User] extends SkinnyControllerBase {

  // -----------------------------------------------------
  // Configuration
  // -----------------------------------------------------

  protected def provider: OAuth2Provider

  protected def clientIdEnvName: String =
    OAuth2LoginFeature.DEFAULT_CLIENT_ID_ENV_NAME_PREFIX + "_" + provider.providerName.toUpperCase(Locale.ENGLISH)

  protected def clientSecretEnvName: String =
    OAuth2LoginFeature.DEFAULT_CLIENT_SECRET_ENV_NAME_PREFIX + "_" + provider.providerName.toUpperCase(Locale.ENGLISH)

  protected def clientId: String = sys.env(clientIdEnvName)

  protected def clientSecret: String = sys.env(clientSecretEnvName)

  protected def generateStateValue(): String = {
    import java.security.MessageDigest
    val digestedBytes = MessageDigest.getInstance("MD5").digest((session.getId + "-" + System.currentTimeMillis).getBytes)
    digestedBytes.map("%02x".format(_)).mkString
  }

  protected def state: String = session.get(OAuth2LoginFeature.SESSION_OAUTH2_STATE_NAME).map(_.toString).getOrElse {
    val state: String = generateStateValue()
    session.setAttribute(OAuth2LoginFeature.SESSION_OAUTH2_STATE_NAME, state)
    state
  }

  protected def scope: String = null

  protected def redirectURI: String

  protected def createAuthenticationRequest(): AuthenticationRequest = {
    val req = AuthenticationRequest(provider)
      .clientId(clientId)
      .responseType(ResponseType.Code)
      .state(state)
      .redirectURI(redirectURI)
    if (scope != null) req.scope(scope) else req
  }

  protected def returnedState: Option[String] = params.get(OAuth.OAUTH_STATE)

  protected def isReturnedStateValid: Boolean = {
    logger.debug(s"OAuth2 state parameter verification -> actual: ${returnedState}, expected: ${state}")
    returnedState.exists(_ == state)
  }

  protected def retrieveNewAccessToken(code: String): OAuth2Token = {
    OAuth2Client.accessToken {
      AccessTokenRequest(provider)
        .grantType(GrantType.AuthorizationCode)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .code(code)
        .redirectURI(redirectURI)
    }.oAuthToken
  }

  protected def retrieveAuthorizedUser(token: OAuth2Token): U

  protected def returnedAuthenticationCode: Option[String] = params.get(OAuth.OAUTH_CODE)

  protected def saveAuthorizedUser(user: U): Unit

  protected def handleWhenCodeNotFound() = handleWhenLoginFailed()

  protected def handleWhenInvalidStateDetected() = handleWhenLoginFailed()

  protected def handleWhenLoginFailed(): Any = haltWithBody(401)

  protected def handleWhenLoginSucceeded(): Any

  // -----------------------------------------------------
  // Actions
  // -----------------------------------------------------

  /**
   * Redirects users to OAuth provider's authentication endpoint.
   */
  def loginRedirect: Any = redirect(createAuthenticationRequest().locationURI)

  /**
   * Accepts callback response from OAuth provider.
   */
  def callback: Any = {
    if (isReturnedStateValid) {
      returnedAuthenticationCode.map { code =>
        logger.debug(s"OAuth2 authorization code: ${code}")
        val token: OAuth2Token = retrieveNewAccessToken(code)
        logger.debug(s"OAuth2 access token: ${toPrettyJSONStringAsIs(token.underlying)}")
        saveAuthorizedUser(retrieveAuthorizedUser(token))
        handleWhenLoginSucceeded()
      }.getOrElse {
        handleWhenCodeNotFound()
      }
    } else {
      handleWhenInvalidStateDetected()
    }
  }
}
