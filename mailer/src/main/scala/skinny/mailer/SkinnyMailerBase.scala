package skinny.mailer

import javax.mail.Session
import javax.mail.internet.InternetAddress
import skinny.mailer.feature._

/**
 * SkinnyMailer base implementation.
 */
trait SkinnyMailerBase {

  self: ConfigFeature with JavaMailSessionFeature with SmtpConfigFeature with ExtraConfigFeature =>

  /**
   * Creates SkinnyMessage object.
   */
  def mail(from: String = config.defaultFrom.orNull[String],
    to: Seq[String] = Nil,
    subject: String = "",
    body: String = "")(implicit s: Session = session): SkinnyMessage = {

    val auth: Option[SmtpAuthentication] = {
      if (smtpConfig.authEnabled) {
        (smtpConfig.user, smtpConfig.password) match {
          case (Some(u), Some(p)) => Some(SmtpAuthentication(u, p))
          case _ => None
        }
      } else None
    }
    val msg = new SkinnyMessage(session, auth, config.transportProtocol)
    Option(from).foreach(f => msg.from = new InternetAddress(f))
    msg.to = to
    msg.mimeVersion = config.mimeVersion
    msg.contentType = s"${config.contentType}; charset=${config.charset}"
    msg.charset = config.charset
    msg.subject = (subject, config.charset)
    msg.body = body
    msg
  }

}
