package skinny.mailer

import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import skinny.mailer.test.SkinnyMailTestSupport
import skinny.mailer.example.MyMailer

class SkinnyMailerSpec extends FlatSpec with ShouldMatchers with SkinnyMailTestSupport with BeforeAndAfter {

  behavior of "SkinnyMailer"

  val toAddress = "to@example.com"

  def inbox = mailbox(toAddress)

  before {
    clearAllMailboxes()
  }

  val mailer = new MyMailer()

  it should "basic" in {
    mailer.sendMessage(toAddress)
    val msg = inbox.receivedMessages.last
    msg.subject.get should equal("test subject 日本語")
    msg.body.get should equal(
      s"""${toAddress} 様
        |
        |いつもご利用ありがとうございます。
        |〜〜をお知らせいたします。
        |
        |""".stripMargin
    )
    msg.cc.size should equal(2)
    msg.bcc.size should equal(1)
  }

  it should "fail sending" in {
    mailer.notSending
    inbox.receivedMessages.size should equal(0)
  }

  it should "sent multiple emails" in {
    mailer.sendMessage(toAddress)
    mailer.sendMessage2(toAddress)

    inbox.size should be === 2
    inbox.receivedMessages.head.subject.get should equal("test subject 日本語")
    inbox.receivedMessages.last.subject.get should equal("subject2")
  }

  it should "send to other address" in {
    inbox.receivedMessages.size should equal(0)
    mailer.sendOther
    inbox.receivedMessages.size should equal(0)
  }

}