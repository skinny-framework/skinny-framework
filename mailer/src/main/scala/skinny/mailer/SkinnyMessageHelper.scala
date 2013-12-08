package skinny.mailer

import javax.mail.{ BodyPart, Message }
import javax.mail.internet.{ MimeBodyPart, MimeMessage }

/**
 * SkinnyMessageHelper provide implicit conversions
 */
trait SkinnyMessageHelper {
  import scala.language.implicitConversions
  implicit def mimeMessageOps[T >: SkinnyMessage <: Message](m: T) = new MimeMessageOps {
    override def instance = m match { case a: MimeMessage => a }
  }

  implicit def mimeBodyPartOps[T >: MimeBodyPart <: BodyPart](b: T) = new MimeBodyPartOps {
    override def instance = b match { case a: MimeBodyPart => a }
  }
}
