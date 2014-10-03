package skinny.oauth2.client

case class OAuth2Token(underlying: org.apache.oltu.oauth2.common.token.OAuthToken) {

  def accessToken: String = underlying.getAccessToken

  def expiresIn: Long = underlying.getExpiresIn

  def refreshToken: String = underlying.getRefreshToken

  def scope: String = underlying.getScope

}
