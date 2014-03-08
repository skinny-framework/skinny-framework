package skinny.mailer

import com.typesafe.config.Config

/**
 * Configuration for SkinnyMailer.
 */
trait SkinnyMailerConfigApi extends SkinnyMailerConfigBase {

  def toCaseClass: SkinnyMailerConfig = SkinnyMailerConfig(
    debug = this.debug,
    mimeVersion = this.mimeVersion,
    charset = this.charset,
    contentType = this.contentType,
    defaultFrom = this.defaultFrom,
    transportProtocol = this.transportProtocol,
    smtp = this.smtp.toCaseClass
  )

  /**
   * Debug mode if true.
   */
  def debug: Boolean = loadedConfig.flatMap(c => opt(c.getBoolean("debug"))).getOrElse(false)

  /**
   * MIME version.
   */
  def mimeVersion: String = loadedConfig.flatMap(c => opt(c.getString("mimeVersion"))).getOrElse("1.0")

  /**
   * Charset.
   */
  def charset: String = loadedConfig.flatMap(c => opt(c.getString("charset"))).getOrElse("UTF-8")

  /**
   * Content-Type header (default: text/plain)
   */
  def contentType: String = loadedConfig.flatMap(c => opt(c.getString("contentType"))).getOrElse("text/plain")

  /**
   * From header default value.
   */
  def defaultFrom: Option[String] = loadedConfig.flatMap(c => opt(c.getString("from")))

  /**
   * Transport protocol.
   */
  def transportProtocol: String = loadedConfig.flatMap(c => opt(c.getString("transport.protocol"))).getOrElse("logging")

  // refs self
  private val self: SkinnyMailerConfigApi = this

  /**
   * SMTP configuration.
   */
  val smtp: SkinnyMailerSmtpConfigApi = new SkinnyMailerSmtpConfigApi {
    override def name: String = self.name
    override def skinnyEnv: String = self.skinnyEnv
    override lazy val loadedConfig: Option[Config] = self.loadedConfig
  }

}

