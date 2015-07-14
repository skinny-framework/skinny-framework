package skinny.mailer.feature

import javax.mail._
import java.util.Properties
import skinny.logging.LoggerProvider
import skinny.mailer.JavaMailOps

/**
 * Provides Java Mail Session and Transport instance.
 */
trait JavaMailSessionFeature extends LoggerProvider {

  self: ConfigFeature with SmtpConfigFeature with ExtraConfigFeature =>

  private[this] def str(a: Any): String = String.valueOf(a)

  private[this] def loadPropertiesForSession() = {
    val props = new Properties()
    props.put("mail.transport.protocol", config.transportProtocol)
    props.put("mail.debug", String.valueOf(config.debug))

    val prefix = s"mail.${config.transportProtocol}"
    props.put(s"${prefix}.debug", str(config.debug))
    props.put(s"${prefix}.host", config.smtp.host)
    props.put(s"${prefix}.port", str(config.smtp.port))
    props.put(s"${prefix}.connectiontimeout", str(config.smtp.connectTimeoutMillis))
    props.put(s"${prefix}.timeout", str(config.smtp.readTimeoutMillis))
    props.put(s"${prefix}.auth", str(config.smtp.authEnabled))
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
    if (config.smtp.authEnabled) config.smtp.passwordAuthenticator match {
      case Some(authenticator) => Session.getInstance(loadPropertiesForSession, authenticator)
      case _ => throw new IllegalStateException("passwordAuthenticator is absent.")
    }
    else Session.getInstance(loadPropertiesForSession)
  }

  /**
   * Provides javax.mail.Transport object with real connection to smtp server.
   */
  def transport: Transport = {
    if (config.transportProtocol == "logging") {
      JavaMailOps.loggingTransport(session)
    } else {
      val transport = session.getTransport(config.transportProtocol)
      if (config.smtp.authEnabled) {
        (config.smtp.user, config.smtp.password) match {
          case (Some(u), Some(p)) => transport.connect(u, p)
          case _ => throw new IllegalStateException("Authentication is required but user/password is absent.")
        }
      } else {
        transport.connect()
      }
      transport
    }
  }

}
