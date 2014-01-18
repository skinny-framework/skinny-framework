package skinny.mailer

import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import org.jvnet.mock_javamail.Mailbox
import java.io.{ InputStreamReader, BufferedReader, InputStream }
import skinny.SkinnyEnv
import skinny.mailer.example.MyMailer2

// TODO wrap Mailbox
import skinny.mailer.implicits.SkinnyMailerImplicits

class WithoutTestSupportSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with SkinnyMailerImplicits {

  // set skinny.env as "test"
  System.setProperty(SkinnyEnv.Key, "test")

  behavior of "SkinnyMailer without TestSupport"

  before {
    inbox.clear()
  }

  def inbox = Mailbox.get(toAddress)

  val toAddress = "to2@example.com"

  it should "send a email" in {
    MyMailer2.mail(
      to = toAddress,
      subject = "test subject 日本語",
      body = "body 日本語"
    ).deliver()

    inbox.size should be(1)
    inbox.get(0).subject should be(Some("test subject 日本語"))
    inbox.get(0).body should be(Some("body 日本語"))
  }

  it should "send a email by using builder" in {
    MyMailer2.to(toAddress).subject("test subject 日本語").body("body 日本語").deliver()

    inbox.size should be(1)
    inbox.get(0).subject.get should be("test subject 日本語")
    inbox.get(0).body.get should be("body 日本語")
  }

  it should "send text only email" in {
    MyMailer2.mail(to = toAddress, body = "text Only").deliver()

    inbox.size should be(1)
    inbox.get(0).body.get should be("text Only")
  }

  it should "send mailer with attachment" in {
    MyMailer2.deliverMessageWithAttachments(toAddress)

    inbox.size should be(1)
    inbox.get(0).body.get should be("foo")
    inbox.get(0).attachments.size should be(3)

    def toString(is: InputStream) = {
      val reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
      (for (s <- reader.readLine()) yield s).foldLeft("") { (a, b) => a + b }
    }
    toString(inbox.get(0).attachments(0).contentStream.get) should be("dummy")
    toString(inbox.get(0).attachments(1).contentStream.get) should be("dummy2")
    toString(inbox.get(0).attachments(2).contentStream.get) should be("dummy3")
  }

  it should "send html mailer" in {
    MyMailer2.deliverHtmlMessage(toAddress)
    inbox.size should be(1)
    inbox.get(0).body.get should be("<h1>Hello!</h3>")
  }

  it should "send emails on a connection" in {
    MyMailer2.deliverMultipleMessages(toAddress)
    inbox.size should be(3)
  }

}
