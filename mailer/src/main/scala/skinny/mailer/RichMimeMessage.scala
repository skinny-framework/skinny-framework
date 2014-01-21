package skinny.mailer

import javax.activation.{ FileDataSource, DataHandler }
import javax.mail._
import javax.mail.Message.RecipientType
import javax.mail.internet.{ InternetAddress, MimeBodyPart, MimeMultipart, MimeMessage }
import scala.collection.JavaConverters._
import java.io.InputStream
import java.util
import org.joda.time.DateTime
import grizzled.slf4j.Logging

/**
 * Enriched MimeMessage .
 */
trait RichMimeMessage extends Logging {

  lazy val mimeMultipart = new MimeMultipart("mixed")

  def underlying: MimeMessage

  var charset = "utf-8"

  private[this] var _contentType = "text/plain"

  // -------------
  // all header lines as an Enumeration of Strings
  // (line is a raw RFC 822 header-line, containing both the "name" and "value" field).

  def allHeaderLines: Seq[String] = underlying.getAllHeaderLines.asScala.map(_.asInstanceOf[String]).toSeq

  // TODO
  def matchingHeaderLines(names: Seq[String]): util.Enumeration[_] = underlying.getMatchingHeaderLines(names.toArray)
  def matchingHeaders(names: Seq[String]): util.Enumeration[_] = underlying.getMatchingHeaders(names.toArray)

  def nonMatchingHeaderLines(names: Seq[String]) = underlying.getNonMatchingHeaderLines(names.toArray)
  def nonMatchingHeaders(names: Seq[String]) = underlying.getNonMatchingHeaders(names.toArray)

  // TODO
  //def allHeaders: util.Enumeration[_] = underlying.getAllHeaders

  // TODO
  val headerLines = new {
    /**
     * Add a raw RFC 822 header-line.
     * @param lines
     * @return
     */
    def ++=(lines: String) = underlying.addHeaderLine(lines)

    /**
     * Add a raw RFC 822 header-line.
     * @param lines
     * @return
     */
    def ++=(lines: Iterable[String]) = lines.foreach(underlying.addHeaderLine)
  }

  // -------------
  // header

  def header: (String) => Array[String] = { key: String => underlying.getHeader(key) }
  def header_=(pair: (String, String)): Unit = underlying.setHeader(pair._1, pair._2)
  def header_=(pairs: Map[String, String]): Unit = pairs.map { case (k, v) => underlying.setHeader(k, v) }

  // -------------
  // from
  // the value of the RFC 822 "From" header fields.
  // If this header field is absent, the "Sender" header field is used.

  // TODO headOption ok?
  def from: Option[Address] = underlying.getFrom.headOption
  def from_=(address: Address): Unit = underlying.setFrom(address)
  def from_=(address: String): Unit = underlying.setFrom(new InternetAddress(address))

  // -------------
  // sender
  // the RFC 822 "Sender" header field.

  def sender = underlying.getSender
  def sender_=(address: String) = underlying.setSender(new InternetAddress(address))
  def sender_=(address: Address) = underlying.setSender(address)

  // -------------
  // recipients
  // all the recipient addresses for the message.
  // Extracts the TO, CC, BCC, and NEWSGROUPS recipients.

  def recipients: (RecipientType) => Seq[Address] = { typ: RecipientType => underlying.getRecipients(typ) }
  def recipients_=(pair: (RecipientType, String)): Unit = underlying.setRecipients(pair._1, pair._2)
  def allRecipients: Seq[Address] = underlying.getAllRecipients

  // -------------
  // to
  // The "To" (primary) recipients.

  def to = recipients(RecipientType.TO)
  def to_=(to: String) = underlying.setRecipients(RecipientType.TO, to)
  def to_=(to: Seq[String]) = underlying.setRecipients(RecipientType.TO, to.mkString(","))

  // -------------
  // bcc
  // The "Bcc" (blind carbon copy) recipients.

  def bcc: Seq[Address] = recipients(RecipientType.BCC)
  def bcc_=(bcc: String) = underlying.setRecipients(RecipientType.BCC, bcc)
  def bcc_=(bcc: Seq[String]) = underlying.setRecipients(RecipientType.BCC, bcc.mkString(","))

  // -------------
  // cc
  // The "Cc" (carbon copy) recipients.

  def cc: Seq[Address] = recipients(RecipientType.CC)
  def cc_=(cc: String) = underlying.setRecipients(RecipientType.CC, cc)
  def cc_=(cc: Seq[String]) = underlying.setRecipients(RecipientType.CC, cc.mkString(","))

  // -------------
  // subject
  // the "Subject" header field.
  //  If the subject is encoded as per RFC 2047, it is decoded and converted into Unicode.
  // If the decoding or conversion fails, the raw data is returned as is.

  def subject: Option[String] = Option(underlying.getSubject)
  def subject_=(subject: String): Unit = underlying.setSubject(subject)
  def subject_=(subjectAndCharset: (String, String)): Unit = subjectAndCharset match {
    case (subject, charset) => underlying.setSubject(subject, charset)
  }

  // -------------
  // text

  def body: Option[String] = underlying.getContent match {
    case s: String => Option(s)
    case mp: Multipart =>
      Option((for { i <- 0 until mp.getCount } yield mp.getBodyPart(i)).withFilter { bp =>
        bp.getContentType match {
          case textReg() => true
          case "text/plain" => true
          case _ => false
        }
      }.withFilter {
        _.getContent match {
          case s: String => true
          case _ => false
        }
      }.map { bp => bp.getContent } mkString ("\n"))
    case _ => None
  }

  private[this] val textReg = """text/[(plain)|(html)].*""".r

  def body_=(text: String) = try {
    underlying.getContent match {
      case s: String => underlying.setText(text, charset)
      case mp: Multipart =>
        val textPart = new MimeBodyPart()
        textPart.setText(text, charset)
        mp.addBodyPart(textPart)
        underlying.setContent(mp)
      case _ =>
    }
  } catch { case e: java.io.IOException => underlying.setText(text, charset) }

  // -------------
  // multipart

  def multipart: Option[MimeMultipart] = underlying.getContent match {
    case mp: MimeMultipart => Some(mp)
    case _ => None
  }

  // -------------
  // content
  // the Message's content to a Multipart object.

  // content is already defined at MimeMessage (returns Array[Byte])
  def contentObject: AnyRef = underlying.getContent

  def contentObject_=(multiPart: Multipart): Unit = underlying.setContent(multiPart)
  // Multipart or new DataHandler(content, mimeType)
  def contentObject_=(content: AnyRef, mimeType: String): Unit = underlying.setContent(content, mimeType)

  // -------------
  // contentID
  // the "Content-ID" header field of this Message.

  def contentID: String = underlying.getContentID
  def contentID_=(id: String): Unit = underlying.setContentID(id)

  // -------------
  // contentLanguage
  // the "Content-Language" header of this MimePart defined by RFC 1766.

  def contentLanguage: Array[String] = underlying.getContentLanguage
  def contentLanguage_=(languages: Seq[String]): Unit = underlying.setContentLanguage(languages.toArray)

  // -------------
  // contentMD5
  // the "Content-MD5" header field of this Message.

  def contentMD5: String = underlying.getContentMD5
  def contentMD5_=(md5: String): Unit = underlying.setContentMD5(md5)

  // -------------
  // contentType
  // the value of the RFC 822 "Content-Type" header field.

  def contentType = _contentType
  def contentType_=(ct: String): Unit = _contentType = ct

  // -------------
  // dataHandler
  // a DataHandler for this Message's content.

  def dataHandler = underlying.getDataHandler
  def dataHandler_=(dh: DataHandler) = underlying.setDataHandler(dh)

  // -------------
  // description
  // the "Content-Description" header field for this Message.

  def description = underlying.getDescription
  def description_=(description: String) = underlying.setDescription(description)

  // -------------
  // disposition
  // the "Content-Disposition" header field for this Message.

  def disposition = underlying.getDisposition
  def disposition_=(disposition: String) = underlying.setDisposition(disposition)

  // -------------
  // encoding
  // the content transfer encoding from the "Content-Transfer-Encoding" header field.

  def encoding = underlying.getEncoding

  // -------------
  // filename
  // the filename associated with this Message.

  def filename = underlying.getFileName
  def filename_=(filename: String) = underlying.setFileName(filename)

  // -------------
  // inputStream

  def inputStream: InputStream = underlying.getInputStream
  def rawInputStream: InputStream = underlying.getRawInputStream

  // -------------
  // lineCount

  def lineCount: Int = underlying.getLineCount

  // -------------
  // messageID

  def messageID: String = underlying.getMessageID

  // -------------
  // mimeVersion

  def mimeVersion: String = underlying.getHeader("MIME-Version").head
  def mimeVersion_=(version: String): Unit = underlying.setHeader("MIME-Version", version)

  // -------------
  // receivedDate
  // Returns null if this date cannot be obtained.

  def receivedDate: Option[DateTime] = Option(underlying.getReceivedDate).map(d => new DateTime(d))

  // -------------
  // sentDate
  // the RFC 822 "Date" header field.
  // Returns null if this field is unavailable or its value is absent.

  def sentDate: Option[DateTime] = Option(underlying.getSentDate).map(d => new DateTime(d))
  def sentDate_=(d: DateTime): Unit = underlying.setSentDate(d.toDate)

  // -------------
  // replyTo
  // the RFC 822 "Reply-To" header field.

  def replyTo: Seq[Address] = underlying.getReplyTo
  def replyTo_=(addresses: String): Unit = { underlying.setReplyTo(InternetAddress.parse(addresses).asInstanceOf[Array[Address]]) }
  def replyTo_=(addresses: Array[_ <: Address]): Unit = underlying.setReplyTo(addresses.asInstanceOf[Array[Address]])

  // -------------
  // size
  // Return the size of the content of this message in bytes.
  def size = underlying.getSize

  // -------------
  // attachments

  val attachments = Attachments(this)

  def addAttachment(filename: String, o: AnyRef, mimeType: String) = {
    restoreText()
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(o, mimeType))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    underlying.setContent(mimeMultipart)
  }

  def addAttachment(filename: String, path: String) = {
    restoreText()
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(new FileDataSource(path)))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    underlying.setContent(mimeMultipart)
  }

  def addAttachment(filename: String, url: java.net.URL) = {
    restoreText()
    val part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(url))
    part.setFileName(filename)
    part.setDisposition("attachment")
    mimeMultipart.addBodyPart(part)
    underlying.setContent(mimeMultipart)
  }

  /**
   * The content is overwritten when adding attachment files, so we need to restore.
   */
  private[this] def restoreText(): Unit = {
    try {
      underlying.getContent match {
        case s: String =>
          val textPart = new MimeBodyPart()
          if (contentType.startsWith("text/html")) textPart.setText(s, charset, "html")
          else textPart.setText(s, charset)
          mimeMultipart.addBodyPart(textPart)
          underlying.setContent(mimeMultipart, contentType)
        case mp: MimeMultipart =>
          underlying.setContent(mp, contentType)
        case other =>
          throw new UnsupportedOperationException(other.getClass.getCanonicalName + " is unexpected.")
      }
    } catch {
      case e: java.io.IOException =>
        logger.warn(s"Failed to read content because ${e.getMessage}", e)
    }
  }

}
