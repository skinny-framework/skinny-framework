package skinny.test

import org.jvnet.mock_javamail.Mailbox
import skinny.mailer.SkinnyMessageHelper

trait SkinnyMailTestSupport extends SkinnyMessageHelper {
  val testMailTo = "to@test.skinny.org"

  def singleMailbox(recipients: String = testMailTo) = Mailbox.get(recipients)
  def clearAll = Mailbox.clearAll()

  import scala.language.implicitConversions
  implicit def convertToMailboxOps(mailbox: Mailbox) = new MailboxOps {
    override def instance: Mailbox = mailbox
  }
}

trait MailboxOps {
  def instance: Mailbox
  def received = for (i <- 0 until instance.size) yield instance.get(i)
}