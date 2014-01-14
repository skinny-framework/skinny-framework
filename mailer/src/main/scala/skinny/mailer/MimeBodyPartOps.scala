package skinny.mailer

import javax.mail.internet.MimeBodyPart
import javax.mail.util.SharedByteArrayInputStream

trait MimeBodyPartOps {
  def instance: MimeBodyPart

  /**
   * if getContent returns type of SharedByteArrayInputStream, get it
   * @return
   */
  def contentStream = instance.getContent match {
    case is: SharedByteArrayInputStream => Some(is)
    case _ => None
  }
}
