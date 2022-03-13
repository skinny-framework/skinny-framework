package skinny.oauth2.client

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BearerRequestSpec extends AnyFlatSpec with Matchers {

  it should "be available" in {
    val req = BearerRequest("http://www.example.com/")
    req.accessToken("aaa") should equal(req)
    req.refreshToken("aaa") should equal(req)
    req.build().body should equal(None)
  }

}
