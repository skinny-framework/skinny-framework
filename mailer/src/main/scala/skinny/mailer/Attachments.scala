package skinny.mailer

import javax.mail.internet.{ MimeBodyPart, MimeMultipart }

/**
 * Attachments for the message.
 *
 * @param message message
 */
case class Attachments(message: RichMimeMessage) extends IndexedSeq[RichMimeBodyPart] {

  def ++=(filename: String, o: AnyRef, mimeType: String): Unit = {
    message.addAttachment(filename, o, mimeType)
  }

  def ++=(filename: String, path: String): Unit = {
    message.addAttachment(filename, path)
  }

  def ++=(filename: String, url: java.net.URL): Unit = {
    message.addAttachment(filename, url)
  }

  def ++=(filename: String, bytes: Array[Byte], mimeType: String): Unit = {
    message.addAttachment(filename, bytes, mimeType)
  }

  override def seq: IndexedSeq[RichMimeBodyPart] = message.underlying.getContent match {
    case mp: MimeMultipart =>
      (for (i <- 0 until mp.getCount()) yield i)
        .map(mp.getBodyPart(_))
        .withFilter(!_.getContent.isInstanceOf[String])
        .map(_.asInstanceOf[MimeBodyPart])
        .map(RichMimeBodyPart(_))

    case _ => IndexedSeq[RichMimeBodyPart]()
  }

  def apply(idx: Int): RichMimeBodyPart = seq.apply(idx)

  def length: Int = seq.size

}
