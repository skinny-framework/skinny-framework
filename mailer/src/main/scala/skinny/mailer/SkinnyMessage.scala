package skinny.mailer

import javax.mail._
import javax.mail.internet._

/**
 * Skinny Message which wraps and extends javax.mail.internet.MimeMessage.
 *
 * @param session session
 */
class SkinnyMessage(session: Session, auth: Option[SmtpAuthentication] = None, transportProtocol: String = "smtp")
    extends MimeMessage(session) with RichMimeMessage {

  def underlying: MimeMessage = this

  /**
   * Connnects to SMTP server.
   * @return
   */
  def connect(): Transport = JavaMailOps.transport(session, auth, transportProtocol)

  /**
   * Actually delivers a message.
   */
  def deliver()(implicit t: Transport = connect()): Unit = deliver(t, false)

  /**
   * Actually delivers a message.
   */
  def deliver(t: Transport, keepConnection: Boolean): Unit = {
    this.saveChanges()
    try t.sendMessage(this, getAllRecipients)
    finally {
      try if (!keepConnection) t.close()
      catch { case e: Exception => }
    }
  }

}
