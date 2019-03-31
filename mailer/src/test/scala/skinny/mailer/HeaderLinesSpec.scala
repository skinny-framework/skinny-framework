package skinny.mailer

import javax.mail.internet.MimeMessage

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar

class HeaderLinesSpec extends FlatSpec with Matchers with MockitoSugar {

  it should "has #++=, #toSeq" in {
    val headerLines = HeaderLines(new RichMimeMessage {
      override def underlying: MimeMessage = mock[MimeMessage]
    })
    headerLines.++=("foo")
    headerLines.++=(Seq("bar", "baz"))
    headerLines.toSeq.size should equal(0) // TODO: better mocking
  }

}
