package skinny.mailer

import javax.mail.{ PasswordAuthentication, Authenticator }

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