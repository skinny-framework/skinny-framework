package skinny.mailer.example

import javax.mail.Session
import java.util.Properties
import skinny.SkinnyEnv
import skinny.mailer.{ SkinnyMailerConfigApi, SkinnyMailer }

object MyMailer2 extends SkinnyMailer {

  implicit override def session = Session.getInstance(new Properties())
  implicit override def transport = session.getTransport("smtp")

  override def config = new SkinnyMailerConfigApi {
    override def skinnyEnv = SkinnyEnv.Test
  }

  def deliverMessageWithAttachments(toAddress: String) = {
    val msg = mail(to = Seq(toAddress))
    msg.body = "foo"
    msg.attachments ++= ("d1.txt", getClass.getResource("/dummy.txt").toURI.getPath)
    msg.attachments ++= ("d2.txt", getClass.getResource("/dummy2.txt").toURI.getPath)
    msg.attachments ++= ("d3.txt", getClass.getResource("/dummy3.txt").toURI.getPath)
    msg.deliver()
  }

  def deliverHtmlMessage(toAddress: String) = {
    val msg = mail()
    msg.from = "from@example.com"
    msg.contentType = "text/html;UTF-8"
    msg.to = toAddress
    msg.body = "<h1>Hello!</h3>"
    msg.deliver()
  }

  def deliverMultipleMessages(toAddress: String) = {
    val t = transport
    val message = mail(to = Seq(toAddress))
    for (i <- 0 until 3) message.deliver(t, true)
    t.close
  }

}
