package controller

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import skinny._

class ControllersSpec extends FlatSpec with Matchers with MockitoSugar {

  it should "mount" in {
    Controllers.mount(mock[ServletContext])
  }

  it should "have urls" in {
    Controllers.root.invalidateUrl should not equal (null)

    Controllers.mail.indexUrl should not equal (null)
    Controllers.mail.sspUrl should not equal (null)

    Controllers.sampleApi.createCompanyUrl should not equal (null)
    Controllers.sampleApi.companiesUrl should not equal (null)

    Controllers.dashboard.indexUrl should not equal (null)

    Controllers.facebook.loginUrl should not equal (null)
    Controllers.facebook.callbackUrl should not equal (null)
    Controllers.facebook.okUrl should not equal (null)

    Controllers.github.loginUrl should not equal (null)
    Controllers.github.callbackUrl should not equal (null)
    Controllers.github.okUrl should not equal (null)

    Controllers.google.loginUrl should not equal (null)
    Controllers.google.callbackUrl should not equal (null)
    Controllers.google.okUrl should not equal (null)

    Controllers.twitter.loginUrl should not equal (null)
    Controllers.twitter.callbackUrl should not equal (null)
    Controllers.twitter.okUrl should not equal (null)

    Controllers.typetalk.loginUrl should not equal (null)
    Controllers.typetalk.callbackUrl should not equal (null)
    Controllers.typetalk.okUrl should not equal (null)

    Controllers.dropbox.loginUrl should not equal (null)
    Controllers.dropbox.callbackUrl should not equal (null)
    Controllers.dropbox.okUrl should not equal (null)
  }

}
