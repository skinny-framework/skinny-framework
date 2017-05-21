package skinny.oauth2.client

import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.common.OAuth

/**
  * Access Token Request (exchange OAuth code for an access token).
  *
  * https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Client+Quickstart
  */
case class AccessTokenRequest(provider: OAuth2Provider) {

  val underlying = new OAuthClientRequest.TokenRequestBuilder(provider.accessTokenEndpoint)

  def param(name: String, value: String): AccessTokenRequest = {
    underlying.setParameter(name, value)
    this
  }

  def state(state: String): AccessTokenRequest = {
    underlying.setParameter(OAuth.OAUTH_STATE, state)
    this
  }

  def grantType(grantType: GrantType): AccessTokenRequest = {
    underlying.setGrantType(grantType.toOltuEnum)
    this
  }

  def clientId(clientId: String): AccessTokenRequest = {
    underlying.setClientId(clientId)
    this
  }

  def clientSecret(clientSecret: String): AccessTokenRequest = {
    underlying.setClientSecret(clientSecret)
    this
  }

  def redirectURI(uri: String): AccessTokenRequest = {
    underlying.setRedirectURI(uri)
    this
  }

  def code(code: String): AccessTokenRequest = {
    underlying.setCode(code)
    this
  }

  def code(code: AuthenticationCode): AccessTokenRequest = this.code(code.value)

  def build(): OAuth2Request = new OAuth2Request(underlying.buildBodyMessage(), Some(provider))

}
