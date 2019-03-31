package skinny.mailer

import javax.mail.Address
import javax.mail.internet.MimeMessage

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar

class RichMimeMessageSpec extends FlatSpec with Matchers with MockitoSugar {

  it should "be available" in {
    val message = new RichMimeMessage {
      override def underlying: MimeMessage = mock[MimeMessage]
    }
    message.allHeaderLines.size should equal(0)
    message.sender = mock[Address]
    message.allRecipients.size should equal(0)
    message.bcc.size should equal(0)
    message.bcc = Nil
    message.bcc = "foo@example.com"
    message.multipart should equal(None)
    message.contentObject should equal(null)
    message.contentObject = null
    message.contentObject_=(null, "text/plain")
    message.dataHandler should equal(null)
    message.dataHandler = null
    message.encoding should equal(null)
    message.inputStream should equal(null)
    message.rawInputStream should equal(null)
    message.lineCount should equal(0)
    message.messageID should equal(null)
    message.receivedDate should equal(None)
    message.size should equal(0)
  }

}
