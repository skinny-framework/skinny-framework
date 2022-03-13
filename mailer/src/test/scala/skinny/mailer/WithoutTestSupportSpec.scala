package skinny.mailer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.jvnet.mock_javamail.Mailbox
import org.scalatest.BeforeAndAfter
import skinny.SkinnyEnv
import skinny.mailer.example.MyMailer2

// TODO wrap Mailbox
import skinny.mailer.implicits.SkinnyMailerImplicits

class WithoutTestSupportSpec extends AnyFlatSpec with Matchers with BeforeAndAfter with SkinnyMailerImplicits {

  // set skinny.env as "test"
  System.setProperty(SkinnyEnv.PropertyKey, "test")

  behavior of "SkinnyMailer without TestSupport"

  before {
    inbox.clear()
  }

  def inbox = Mailbox.get(toAddress)

  val toAddress = "to2@example.com"

  it should "send a email" in {
    MyMailer2
      .mail(
        to = Seq(toAddress),
        subject = "test subject 日本語",
        body = "body 日本語"
      )
      .deliver()

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
    MyMailer2.mail(to = Seq(toAddress), body = "text Only").deliver()

    inbox.size should be(1)
    inbox.get(0).body.get should be("text Only")
  }

  it should "send mailer with attachment" in {
    MyMailer2.deliverMessageWithAttachments(toAddress)

    inbox.size should be(1)

    // This assertion fails on Java 8
    // https://java.net/jira/browse/MOCK_JAVAMAIL-14
    /*
[info] - should send mailer with attachment *** FAILED ***
[info]   "foo[
[info] dummy
[info] dummy2
[info] dummy3]" was not equal to "foo[]" (WithoutTestSupportSpec.scala:59)
     */
    //inbox.get(0).body.get should be("foo")
    inbox.get(0).body.get should startWith("foo")

    // This assertion fails on Java 8
    // https://java.net/jira/browse/MOCK_JAVAMAIL-14
    /*
[info] - should send mailer with attachment *** FAILED ***
[info]   0 was not equal to 3 (WithoutTestSupportSpec.scala:69)
     */
    //inbox.get(0).attachments.size should be(3)
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
