package skinny.mailer

import java.util.Properties
import javax.mail._
import javax.mail.internet.InternetAddress
import com.typesafe.config.ConfigFactory
import skinny.SkinnyEnv

trait SkinnyMailer extends SkinnyMailerBase with SkinnyMessageHelper { config: SkinnyMailerConfig =>

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

trait SkinnyMailerConfig {
  def debug = false
  def mimeVersion = "1.0"
  def charset = "UTF-8"
  def contentType = "text/plain"

  /*
  smtp configure
   */
  def smtpHost = "smtp.skinny.org"
  def smtpPort = 587
  def smtpConnectionTimeout = 600
  def smtpTimeout = 60

  /*
  smtps configure
   */
  def smtpAuth = true
  def smtpStartTLSEnable = true

  /*
  transport configure
   */
  def transportProtocol = "smtps"

  /*
  mailer configure
   */
  def smtpUser = "skinny"
  def smtpPassword = "password"

  /*
  message configure
   */
  def defaultFrom = "no-reply@skinny.org"

  /**
   *
   * @return
   */
  def passwordAuthenticator = new Authenticator {
    override def getPasswordAuthentication = {
      new PasswordAuthentication(smtpUser, smtpPassword)
    }
  }
}

trait SkinnyDefaultMailerConfig extends SkinnyMailerConfig {
  def configName = "default"
  def skinnyEnv = SkinnyEnv.get().getOrElse(SkinnyEnv.Development)
  val rootConf = ConfigFactory.load()
  val mailConfigPath = s"${skinnyEnv}.mailer.${configName}"
  val conf = rootConf.getConfig(mailConfigPath)

  override def debug = conf.getBoolean("debug")
  override def mimeVersion = conf.getString("mimeVersion")
  override def charset = conf.getString("charset")
  override def contentType = conf.getString("contentType")
  override def defaultFrom = conf.getString("from")
  override def smtpHost = conf.getString("smtp.host")
  override def smtpPort = conf.getInt("smtp.port")
  override def smtpConnectionTimeout = conf.getInt("smtp.connectionTimeout")
  override def smtpTimeout = conf.getInt("smtp.timeout")
  override def smtpAuth = conf.getBoolean("smtp.auth")
  override def smtpStartTLSEnable = conf.getBoolean("smtp.starttls.enable")

  override def transportProtocol = conf.getString("transportProtocol")

  override def smtpUser = conf.getString("user")
  override def smtpPassword = conf.getString("password")
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