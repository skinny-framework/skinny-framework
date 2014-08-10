package skinny.oauth2.client

import javax.servlet.http.HttpServletRequest

import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse

/**
 * Authorization Code from redirect URI
 * @param value
 */
case class AuthenticationCode(value: String)

object AuthenticationCode {

  def from(request: HttpServletRequest): Option[AuthenticationCode] = {
    Option(OAuthAuthzResponse.oauthCodeAuthzResponse(request)).map(res => AuthenticationCode(res.getCode))
  }

}
