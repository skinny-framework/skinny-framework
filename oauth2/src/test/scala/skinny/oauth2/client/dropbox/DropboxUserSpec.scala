package skinny.oauth2.client.dropbox

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DropboxUserSpec extends AnyFlatSpec with Matchers {

  it should "work" in {
    val raw = new RawDropboxUser(
      uid = 123L,
      displayName = "foo",
      nameDetails = new NameDetails(
        familiarName = "f",
        givenName = "g",
        surname = "s"
      ),
      referralLink = "link",
      country = "Japan",
      locale = "ja",
      email = "foo@example.com",
      emailVerified = true,
      isPaired = false,
      team = None,
      quotaInfo = new QuotaInfo(
        datastores = 1L,
        shared = 1L,
        quota = 1L,
        normal = 1L
      )
    )
    raw.toDropboxUser.id should equal("123")
  }

}
