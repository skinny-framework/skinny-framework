package skinny.mailer

import org.scalatest.{ FlatSpec }
import org.scalatest.matchers.ShouldMatchers
import org.jvnet.mock_javamail.Mailbox
import java.io.{ InputStreamReader, BufferedReader, InputStream }

import skinny.SkinnyEnv

class SkinnyMailerSpec extends FlatSpec with ShouldMatchers with SkinnyMessageHelper {
  behavior of "SkinnyMailer"

  val singleMailTo = "to@example.com"

  object TestMailer extends SkinnyMailer with SkinnyMailerConfig with SkinnyMockMailer {
    def send_mail = {
      mail(to = singleMailTo,
        subject = "test subject 日本語",
        text = "body 日本語")
        .deliver
    }

    def textOnlyMessage = mail(to = singleMailTo, text = "text Only").deliver

    def messageWithAttachment = {
      val msg = mail(to = singleMailTo)
      msg.text = "foo"
      msg.attachment ++= ("d1.txt", getClass.getResource("/dummy.txt").toURI.getPath)
      msg.attachment ++= ("d2.txt", getClass.getResource("/dummy2.txt").toURI.getPath)
      msg.attachment ++= ("d3.txt", getClass.getResource("/dummy3.txt").toURI.getPath)
      msg.deliver
    }

    def htmlMessage = {
      val msg = message
      msg.from = "from@example.com"
      msg.contentType = "text/html;UTF-8"
      msg.to = singleMailTo
      msg.text = "<h1>Hello!</h3>"
      msg.deliver
    }

    def templateMessage = mail(to = singleMailTo,
      subject = "this mail was created by ssp",
      text = ssp(getClass.getResource("/").getPath + "test", Map("name" -> "Skinny framework"))
    ).deliver

    def sendingEmails = {
      val t = transport
      val message = mail(to = singleMailTo)

      for (i <- 0 until 3) message.deliver(t, true)
      t.close
    }
  }

  it should "write body with ssp" in {
    Mailbox.clearAll()
    TestMailer.templateMessage
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(1)
    inbox.get(0).text should be("Hello Skinny framework!")
  }

  it should "send a email" in {
    Mailbox.clearAll()
    TestMailer.send_mail
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(1)
    inbox.get(0).getSubject should be("test subject 日本語")
    inbox.get(0).text should be("body 日本語")
  }

  it should "send text only email" in {
    Mailbox.clearAll()
    TestMailer.textOnlyMessage
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(1)
    inbox.get(0).text should be("text Only")
  }

  it should "send mailer with attachment" in {
    Mailbox.clearAll()
    TestMailer.messageWithAttachment
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(1)

    inbox.get(0).text should be("foo")

    val files = inbox.get(0).attachments
    files.size should be(3)

    def isToString(is: InputStream) = {
      val reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
      (for (s <- reader.readLine()) yield s).foldLeft("") { (a, b) => a + b }
    }
    isToString(files(0).contentStream.get) should be("dummy")
    isToString(files(1).contentStream.get) should be("dummy2")
    isToString(files(2).contentStream.get) should be("dummy3")
  }

  it should "send html mailer" in {
    Mailbox.clearAll()
    TestMailer.htmlMessage
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(1)
    inbox.get(0).text should be("<h1>Hello!</h3>")
  }

  it should "send emails on a connection" in {
    Mailbox.clearAll()
    TestMailer.sendingEmails
    val inbox = Mailbox.get(singleMailTo)
    inbox.size should be(3)
  }

  class ConfigTestMailer extends SkinnyDefaultMailer {
    override def skinnyEnv = SkinnyEnv.Test
  }

  it should "be available configure" in {
    new ConfigTestMailer().debug should be(true)
  }
}