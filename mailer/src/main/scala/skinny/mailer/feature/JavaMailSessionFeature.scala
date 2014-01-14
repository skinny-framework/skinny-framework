package skinny.mailer.feature

import java.util.Properties
import javax.mail.{ Transport, Session }

/**
 * Provides Java Mail Session and Transport instance.
 */
trait JavaMailSessionFeature {

  self: ConfigFeature with SmtpConfigFeature with ExtraConfigFeature =>

  private[this] def str(a: Any): String = String.valueOf(a)

  private[this] def loadPropertiesForSession() = {
    val props = new Properties()
    props.put("mail.transport.protocol", config.transportProtocol)
    props.put("mail.debug", String.valueOf(config.debug))

    val prefix = if (config.smtp.host.endsWith(".gmail.com")) "mail.smtps" else "mail.smtp"
    props.put(s"${prefix}.debug", str(config.debug))
    props.put(s"${prefix}.host", smtpConfig.host)
    props.put(s"${prefix}.port", str(smtpConfig.port))
    props.put(s"${prefix}.connectiontimeout", str(smtpConfig.connectTimeoutMillis))
    props.put(s"${prefix}.timeout", str(smtpConfig.readTimeoutMillis))
    props.put(s"${prefix}.auth", str(smtpConfig.authEnabled))
    props.put(s"${prefix}.starttls.enable", str(config.transportProtocol))

    extraConfig.properties.foreach {
      case (k, v) => props.put(k, str(v))
    }
    props
  }

  /**
   * Provides javax.mail.Session object with loaded basicConfiguration.
   * This object doesn't have a connection to smtp server.
   */
  def session: Session = {
    if (smtpConfig.authEnabled) smtpConfig.passwordAuthenticator match {
      case Some(authenticator) => Session.getInstance(loadPropertiesForSession, authenticator)
      case _ => throw new IllegalStateException("passwordAuthenticator is absent.")
    }
    else Session.getInstance(loadPropertiesForSession)
  }

  /**
   * Provides javax.mail.Transport object with real connection to smtp server.
   */
  def transport: Transport = {
    val transport = session.getTransport(config.transportProtocol)
    if (smtpConfig.authEnabled) {
      (smtpConfig.user, smtpConfig.password) match {
        case (Some(u), Some(p)) => transport.connect(u, p)
        case _ => throw new IllegalStateException("Authentication is required but user/password is absent.")
      }
    } else {
      transport.connect()
    }
    transport
  }

}
