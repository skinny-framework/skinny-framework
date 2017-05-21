package skinny.mailer

import javax.mail._
import javax.mail.internet._

/**
  * Skinny Message which wraps and extends javax.mail.internet.MimeMessage.
  *
  * @param currentSession session
  */
class SkinnyMessage(val currentSession: Session,
                    val auth: Option[SmtpAuthentication] = None,
                    val transportProtocol: String = "smtp")
    extends MimeMessage(currentSession)
    with RichMimeMessage {

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
      catch { case scala.util.control.NonFatal(_) => }
    }
  }

}
