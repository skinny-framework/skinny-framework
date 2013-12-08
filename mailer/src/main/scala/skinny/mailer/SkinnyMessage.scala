package skinny.mailer

import javax.mail._
import javax.mail.internet._
import javax.activation.{ FileDataSource, DataHandler }

class SkinnyMessage(session: Session) extends MimeMessage(session) { self =>
  val mimeMultipart = new MimeMultipart("mixed")

  /**
   * Sending.
   * after sending email, the transport will be closed.
   * @param t
   */
  def deliver(implicit t: Transport): Unit = deliver(t, false)

  /**
   * Sending.
   *
   * @param t
   * @param keepConnection keep transport connection or not
   */
  def deliver(t: Transport, keepConnection: Boolean): Unit = {
    try {
      t.sendMessage(self, self.getAllRecipients)
    } finally {
      if (!keepConnection) t.close()
    }
  }

  val attachment = new {
    /**
     * Add attachment file.
     * @param filename
     * @param o
     * @param mime
     */
    def ++=(filename: String, o: Any, mime: String) = self.addAttachment(filename, o, mime)

    /**
     * Add attachment file.
     * @param filename
     * @param path
     */
    def ++=(filename: String, path: String) = self.addAttachment(filename, path)

    /**
     * Add attachment file.
     * @param filename
     * @param url
     */
    def ++=(filename: String, url: java.net.URL) = self.addAttachment(filename, url)
  }

  /**
   * Add attachment file.
   * @param filename
   * @param o
   * @param mime
   */
  def addAttachment(filename: String, o: Any, mime: String) = {
    restoreText
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(o, mime))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    this.setContent(mimeMultipart)
  }

  /**
   * Add attachment file
   * @param filename
   * @param path
   */
  def addAttachment(filename: String, path: String) = {
    restoreText
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(new FileDataSource(path)))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    this.setContent(mimeMultipart)
  }

  /**
   * Add attachment file
   * @param filename
   * @param url
   */
  def addAttachment(filename: String, url: java.net.URL) = {
    restoreText
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(url))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    this.setContent(mimeMultipart)
  }

  /**
   * when add attachment files, the contents will be overwrite. so need to restore it.
   */
  private[this] def restoreText = {
    try {
      getContent match {
        case s: String => {
          val textPart = new MimeBodyPart()
          textPart.setText(s)
          mimeMultipart.addBodyPart(textPart)
          this.setContent(mimeMultipart)
        }
        case _ =>
      }
    } catch {
      case e: java.io.IOException =>
    }
  }
}