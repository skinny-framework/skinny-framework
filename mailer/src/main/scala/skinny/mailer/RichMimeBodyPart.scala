package skinny.mailer

import javax.mail.internet.MimeBodyPart
import javax.mail.util.SharedByteArrayInputStream

/**
  * Enriched MimeBodyPart.
  */
case class RichMimeBodyPart(underlying: MimeBodyPart) {

  /**
    * if getContent returns type of SharedByteArrayInputStream, get it
    * @return
    */
  def contentStream: Option[SharedByteArrayInputStream] = underlying.getContent match {
    case is: SharedByteArrayInputStream => Some(is)
    case _                              => None
  }

}
