package skinny.mailer

import javax.mail.internet.{ InternetAddress, MimeBodyPart, MimeMultipart, MimeMessage }
import scala.collection.immutable
import javax.mail.Message.RecipientType
import javax.mail.{ Address, Multipart }
import javax.activation.DataHandler
import java.util.Date

trait MimeMessageOps {
  def instance: MimeMessage

  /**
   * Set mail text
   * @param text
   */
  def text_=(text: String) = try {
    instance.getContent match {
      case s: String => {
        instance.setText(text, "utf-8")
      }
      case mp: Multipart => {
        val textPart = new MimeBodyPart()
        textPart.setText(text, "utf-8")
        mp.addBodyPart(textPart)
        instance.setContent(mp)
      }
      case _ =>
    }
  } catch {
    case e: java.io.IOException => instance.setText(text, "utf-8")
  }

  /**
   * returns mail text.
   * @return
   */
  def text = textOption.getOrElse("")

  val textReg = """text/[(plain)|(html)].*""".r
  /**
   * returns mail text optionally.
   * @return
   */
  def textOption = instance.getContent match {
    case s: String => Some(s)
    case mp: Multipart => Some((for { i <- 0 until mp.getCount } yield mp.getBodyPart(i))
      .withFilter { bp =>
        bp.getContentType match {
          case textReg() => true
          case "text/plain" => true
          case _ => false
        }
      } withFilter {
        _.getContent match {
          case s: String => true
          case _ => false
        }
      } map { bp => bp.getContent } mkString ("\n"))
    case _ => None
  }

  /**
   *
   * @return
   */
  def multipartOption = instance.getContent match {
    case mp: MimeMultipart => Some(mp)
    case _ => None
  }

  /**
   *
   * get attachment files.
   * @return
   */
  def attachments: immutable.IndexedSeq[MimeBodyPart] = attachmentsOption
    .getOrElse(immutable.IndexedSeq.empty[MimeBodyPart])

  /**
   * Optionally returns attachment files.
   * @return
   */
  def attachmentsOption: Option[immutable.IndexedSeq[MimeBodyPart]] = instance.getContent match {
    case mp: MimeMultipart => {
      Some((for { i <- 0 until mp.getCount() } yield i).map {
        mp.getBodyPart(_)
      }.withFilter {
        !_.getContent.isInstanceOf[String]
      }.map {
        _.asInstanceOf[MimeBodyPart]
      })
    }
    case _ => None
  }

  val headerLines = new {
    /**
     * Add a raw RFC 822 header-line.
     * @param lines
     * @return
     */
    def ++=(s: String) = instance.addHeaderLine(s)

    /**
     * Add a raw RFC 822 header-line.
     * @param lines
     * @return
     */
    def ++=(lines: Iterable[String]) = lines.foreach(instance.addHeaderLine)
  }

  /**
   * Get rows RFC 822 header-line.
   * @return
   */
  def allHeaderLines = instance.getAllHeaderLines
  def allHeaders = instance.getAllHeaders
  def allRecipients = instance.getAllRecipients

  /**
   * Add recipients as bcc
   * @param cc
   */
  def bcc_=(cc: String) = instance.setRecipients(RecipientType.CC, cc)

  /**
   * Get bcc recipiebts
   * @return
   */
  def bcc = recipients(RecipientType.BCC)

  /**
   * Add recipients as cc
   * @param cc
   */
  def cc_=(cc: String) = instance.setRecipients(RecipientType.CC, cc)

  /**
   * Get cc recipients
   * @return
   */
  def cc = recipients(RecipientType.CC)

  /**
   * This method sets the Message's content to a Multipart object.
   * @param mp
   * @return
   */
  def content_=(mp: Multipart) = instance.setContent(mp)

  /**
   * A convenience method for setting this Message's content.
   * @param o
   * @return
   */
  def content_=(o: (AnyRef, String)) = instance.setContent(o._1, o._2)

  def contents_=(mp: Multipart) = instance.setContent(mp)
  def contents: Any = instance.getContent

  /**
   *
   * @param cid
   */
  def contentID_=(cid: String) = instance.setContentID(cid)
  def contentID = instance.getContentID

  /**
   * Set the "Content-Language" header of this MimePart.
   * @param languages
   * @return
   */
  def contentLanguage_=(languages: Array[String]) = instance.setContentLanguage(languages)
  def contentLanguage = instance.getContentLanguage

  /**
   * Set the "Content-MD5" header field of this Message.
   * @param md5
   * @return
   */
  def contentMD5_=(md5: String) = instance.setContentMD5(md5)
  def contentMD5 = instance.getContentMD5

  /**
   * Set content-type
   * @param contentType
   */
  def contentType_=(contentType: String) = instance.setHeader("Content-Type", contentType)
  def contentType = instance.getContentType

  /**
   *
   * @param dh
   */
  def dataHandler_=(dh: DataHandler) = instance.setDataHandler(dh)
  def dataHandler = instance.getDataHandler

  /**
   * Set the "Content-Description" header field for this Message.
   * @param description
   * @return
   */
  def description_=(description: String) = instance.setDescription(description)
  def description = instance.getDescription

  /**
   * Set the "Content-Disposition" header field for this Message.
   * @param disposition
   * @return
   */
  def disposition_=(disposition: String) = instance.setDisposition(disposition)
  def disposition = instance.getDisposition

  def encoding = instance.getEncoding

  /**
   * Set the filename associated with this part, if possible.
   * @param filename
   */
  def filename_=(filename: String) = instance.setFileName(filename)
  def filename = instance.getFileName

  /**
   * Add the specified addresses to the existing "From" field.
   * @param address
   * @return
   */
  def from_=(address: Address) = instance.setFrom(address)
  def from_=(address: String) = instance.setFrom(new InternetAddress(address))
  def from = instance.getFrom

  /**
   *
   * @param pair
   */
  def header_=(pair: (String, String)) = instance.setHeader(pair._1, pair._2)
  def header_=(pairs: Map[String, String]) = pairs.map { case (k, v) => instance.setHeader(k, v) }
  def header = { key: String => instance.getHeader(key) }

  //  def header = { (name:String, delimiter:String) => instance.getHeader(name, delimiter) }
  /**
   *
   * @return
   */
  def inputStream = instance.getInputStream

  /**
   *
   * @return
   */
  def lineCount = instance.getLineCount

  /**
   *
   * @param names
   * @return
   */
  def matchingHeaderLines(names: Array[String]) = instance.getMatchingHeaderLines(names)
  def matchingHeaders(names: Array[String]) = instance.getMatchingHeaders(names)

  /**
   *
   * @param msgnum
   */
  def messageID = instance.getMessageID

  def mimeVersion_=(version: String) = instance.setHeader("MIME-Version", version)
  def mimeVersion = instance.getHeader("MIME-Version").head

  /**
   *
   * @param names
   * @return
   */
  def nonMatchingHeaderLines(names: Array[String]) = instance.getNonMatchingHeaderLines(names)
  def nonMatchingHeaders(names: Array[String]) = instance.getNonMatchingHeaders(names)

  /**
   *
   * @return
   */
  def rawInputStream = instance.getRawInputStream

  /**
   *
   * @return
   */
  def receivedDate = instance.getReceivedDate

  /**
   * the specified recipient type to the given addresses.
   * @param pair
   */
  def recipients_=(pair: (RecipientType, String)) = instance.setRecipients(pair._1, pair._2)
  // def recipients_=(pair:(RecipientType, Address)) = instance.setRecipient(pair._1, pair._2)
  def recipients = { typ: RecipientType => instance.getRecipients(typ) }

  /**
   * Set the RFC 822 "Reply-To" header field.
   * @param addresses
   */
  def replyTo_=(addresses: String) { instance.setReplyTo(InternetAddress.parse(addresses).asInstanceOf[Array[Address]]) }
  def replyTo_=(addresses: Array[_ <: Address]) = instance.setReplyTo(addresses.asInstanceOf[Array[Address]])
  def replyTo = instance.getReplyTo

  /**
   * Set the RFC 822 "Sender" header field.
   * @param address
   * @return
   */
  def sender_=(address: String) = instance.setSender(new InternetAddress(address))
  def sender_=(address: Address) = instance.setSender(address)
  def sender = instance.getSender

  /**
   * Set the RFC 822 "Date" header field.
   * @param d
   * @return
   */
  def sentDate_=(d: Date) = instance.setSentDate(d)
  def sentDate = instance.getSentDate

  /**
   *
   * @return
   */
  def size = instance.getSize

  /**
   * Set the "Subject" header field.
   * @param subject
   */
  def subject_=(subject: String) = instance.setSubject(subject)
  def subject_=(subject: (String, String)) = instance.setSubject(subject._1, subject._2)
  def subject = instance.getSubject

  /**
   * Set To
   * @param to
   */
  def to_=(to: String) = instance.setRecipients(RecipientType.TO, to)
  def to = recipients(RecipientType.TO)
}
