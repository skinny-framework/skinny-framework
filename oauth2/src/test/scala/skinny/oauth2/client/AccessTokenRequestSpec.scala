package skinny.oauth2.client

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AccessTokenRequestSpec extends AnyFlatSpec with Matchers {

  it should "be available" in {
    val req = new AccessTokenRequest(OAuth2Provider.GitHub)
    req.param("foo", "bar") should equal(req)
    req.state("xxxyyy") should equal(req)
    req.grantType(GrantType.AuthorizationCode) should equal(req)
    req.clientId("123") should equal(req)
    req.clientSecret("secret") should equal(req)
    req.redirectURI("http://www.example.com/") should equal(req)
    req.code("codeeeee") should equal(req)
    req.code(AuthenticationCode("xxxxzzz")) should equal(req)
    req.build().body should (
      equal(
        Some(
          "client_secret=secret&grant_type=authorization_code&redirect_uri=http%3A%2F%2Fwww.example.com%2F&state=xxxyyy&foo=bar&code=xxxxzzz&client_id=123"
        )
      )
      or
      equal(
        Some(
          "code=xxxxzzz&grant_type=authorization_code&foo=bar&state=xxxyyy&client_secret=secret&redirect_uri=http%3A%2F%2Fwww.example.com%2F&client_id=123"
        )
      )
    )
  }

}
