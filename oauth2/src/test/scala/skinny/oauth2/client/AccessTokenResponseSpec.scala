package skinny.oauth2.client

import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse
import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._

class AccessTokenResponseSpec extends FlatSpec with Matchers with MockitoSugar {

  it should "be available" in {
    val oauthResponse = mock[OAuthAccessTokenResponse]
    when(oauthResponse.getBody).thenReturn("foo")
    when(oauthResponse.getAccessToken).thenReturn("token")
    when(oauthResponse.getExpiresIn).thenReturn(123L)
    when(oauthResponse.getRefreshToken).thenReturn("refresh")
    when(oauthResponse.getScope).thenReturn("profile")
    val response = new AccessTokenResponse(oauthResponse)
    response.body should equal("foo")
    response.oAuthToken should equal(OAuth2Token(oauthResponse.getOAuthToken))
    response.accessToken should equal("token")
    response.expiresIn should equal(123L)
    response.refreshToken should equal("refresh")
    response.scope should equal("profile")
  }

}
