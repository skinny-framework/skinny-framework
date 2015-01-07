package skinny.oauth2.client

import org.scalatest._

class BearerRequestSpec extends FlatSpec with Matchers {

  it should "be available" in {
    val req = BearerRequest("http://www.example.com/")
    req.accessToken("aaa") should equal(req)
    req.refreshToken("aaa") should equal(req)
    req.build().body should equal(None)
  }

}
