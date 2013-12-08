package skinny.test

import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import skinny.mailer._
import javax.mail.MessagingException

class SkinnyMailTestSupportSpec extends FlatSpec with ShouldMatchers with SkinnyMailTestSupport with BeforeAndAfter {
  behavior of "SkinnyMailTestSupportSpec"
  override val testMailTo = "to@example.com"

  before {
    clearAll
  }
  after {
    clearAll
  }

  object MyMailer extends SkinnyMailer with SkinnyMailerConfig with SkinnyMockMailer {
    def sendMessage = {
      mail(to = testMailTo,
        subject = "test subject 日本語",
        text = "body 日本語").deliver
    }

    def sendMessage2 = {
      mail(to = testMailTo,
        subject = "subject2",
        text = "body2").deliver
    }

    def sendOther = {
      mail(to = "to2@example.com").deliver
    }

    def notSending: Either[MessagingException, SkinnyMessage] = Left(new MessagingException())
  }

  it should "basic" in {
    val box = singleMailbox()
    MyMailer.sendMessage
    val msg = box.received.last

    msg.getSubject should be("test subject 日本語")
    msg.text should be("body 日本語")

  }

  it should "failed to send" in {
    val box = singleMailbox()
    MyMailer.notSending
    box.received.size should be === 0
  }

  it should "sent emails" in {
    val box = singleMailbox()
    MyMailer.sendMessage
    MyMailer.sendMessage2

    singleMailbox().size should be === 2
    box.received.head.subject should be === "test subject 日本語"
    box.received.last.subject should be === "subject2"

  }

  it should "send to other" in {
    val box = singleMailbox()
    box.received.size should be === 0
    MyMailer.sendOther
    box.received.size should be === 0
  }
}