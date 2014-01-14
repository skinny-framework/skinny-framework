package skinny.mailer

import java.util.Properties
import javax.mail._
import javax.mail.internet.InternetAddress
import com.typesafe.config.ConfigFactory
import skinny.SkinnyEnv
import skinny.mailer.implicits.SkinnyMessageImplicits

trait SkinnyMailer extends SkinnyMailerBase with SkinnyMessageImplicits { config: SkinnyMailerConfig =>

  override def properties = {
    val properties = new Properties()
    properties.put("mail.debug", String.valueOf(config.debug))

    properties.put("mail.smtp.host", config.smtpHost)
    properties.put("mail.host", config.smtpHost)
    properties.put("mail.smtp.port", String.valueOf(config.smtpPort))
    properties.put("mail.smtp.connectiontimeout", String.valueOf(config.smtpConnectionTimeout))
    properties.put("mail.smtp.timeout", String.valueOf(config.smtpTimeout))

    // smtps
    properties.put("mail.smtp.auth", String.valueOf(config.smtpAuth))
    properties.put("mail.transport.protocol", config.transportProtocol)
    properties.put("mail.smtp.starttls.enable", String.valueOf(config.transportProtocol))

    properties
  }

  implicit override def session = {
    if (config.smtpAuth) {
      Session.getInstance(properties, passwordAuthenticator)
    } else {
      Session.getInstance(properties)
    }
  }

  implicit override def transport = {
    val t = session.getTransport(config.transportProtocol)
    if (config.smtpAuth) {
      t.connect(config.smtpUser, config.smtpPassword)
    } else {
      t.connect()
    }
    t
  }

  def message(implicit session: Session) = {
    val msg = new SkinnyMessage(session)
    msg.mimeVersion = config.mimeVersion
    msg.contentType = "%s;%s".format(config.contentType, config.charset)
    msg
  }

  def mail(to: String, from: String = config.defaultFrom, subject: String = "", text: String = "") = {
    val msg = message
    msg.subject = (subject, charset)
    msg.from = new InternetAddress(from)
    msg.to = to
    msg.text = text
    msg
  }
}

trait SkinnyMailerBase extends ScalateSkinnyMailerSupport {
  def properties: Properties
  implicit def session: Session
  implicit def transport: Transport
}

/**
 * SkinnyMockMailer provide mock Session and mock Transport
 */
trait SkinnyMockMailer extends SkinnyMailerBase {
  override def properties = new Properties()
  implicit override def session = Session.getInstance(properties)
  implicit override def transport = session.getTransport("smtp")
}

trait SkinnyDefaultMailer extends SkinnyMailer with SkinnyDefaultMailerConfig