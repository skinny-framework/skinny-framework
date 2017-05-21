package skinny.mailer.implicits

import scala.language.implicitConversions

import javax.mail._
import javax.mail.internet._
import skinny.mailer._

/**
  * Implicit conversions for SkinnyMailer.
  */
trait SkinnyMailerImplicits {

  implicit def convertMimeMessageToRichMimeMessage[T >: SkinnyMessage <: Message](m: T): RichMimeMessage = m match {
    case m: MimeMessage =>
      new RichMimeMessage {
        override def underlying = m
      }
  }

  implicit def convertMimeBodyPartToRichMimeBodyPart[T >: MimeBodyPart <: BodyPart](b: T): RichMimeBodyPart = b match {
    case b: MimeBodyPart => new RichMimeBodyPart(b)
  }

}
