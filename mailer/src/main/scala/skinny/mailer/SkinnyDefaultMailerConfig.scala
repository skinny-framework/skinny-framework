package skinny.mailer

import skinny.SkinnyEnv
import com.typesafe.config.ConfigFactory

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
