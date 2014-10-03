package skinny.oauth2.client

import org.apache.oltu.oauth2.client.request._
import org.apache.oltu.oauth2.common.OAuth

/**
 * Authorization Request (build OAuth End User Authorization Request).
 *
 * https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Client+Quickstart
 */
case class AuthenticationRequest(provider: OAuth2Provider) {

  val underlying = new OAuthClientRequest.AuthenticationRequestBuilder(provider.authorizationEndpoint)

  def param(name: String, value: String): AuthenticationRequest = {
    underlying.setParameter(name, value)
    this
  }

  def state(state: String): AuthenticationRequest = {
    underlying.setParameter(OAuth.OAUTH_STATE, state)
    this
  }

  def responseType(responseType: String): AuthenticationRequest = {
    underlying.setResponseType(responseType)
    this
  }

  def responseType(responseType: ResponseType): AuthenticationRequest = this.responseType(responseType.value)

  def scope(scope: String): AuthenticationRequest = {
    underlying.setScope(scope)
    this
  }

  def clientId(clientId: String): AuthenticationRequest = {
    underlying.setClientId(clientId)
    this
  }

  def redirectURI(uri: String): AuthenticationRequest = {
    underlying.setRedirectURI(uri)
    this
  }

  def locationURI: String = underlying.buildQueryMessage().getLocationUri

}

