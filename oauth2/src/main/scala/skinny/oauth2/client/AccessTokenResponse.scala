package skinny.oauth2.client

import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse

/**
  * Access Token Response.
  */
case class AccessTokenResponse(underlying: OAuthAccessTokenResponse) {

  def oAuthToken: OAuth2Token = OAuth2Token(underlying.getOAuthToken)
  def accessToken: String     = underlying.getAccessToken
  def expiresIn: Long         = underlying.getExpiresIn
  def refreshToken: String    = underlying.getRefreshToken
  def scope: String           = underlying.getScope

  def body: String = underlying.getBody

}
