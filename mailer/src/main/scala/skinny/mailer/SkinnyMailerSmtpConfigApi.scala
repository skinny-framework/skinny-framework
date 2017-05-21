package skinny.mailer

import javax.mail.{ Authenticator, PasswordAuthentication }

/**
  * SMTP settings for SkinnyMailer.
  */
trait SkinnyMailerSmtpConfigApi extends SkinnyMailerConfigBase {

  def toCaseClass: SkinnyMailerSmtpConfig = SkinnyMailerSmtpConfig(
    host = this.host,
    port = this.port,
    connectTimeoutMillis = this.connectTimeoutMillis,
    readTimeoutMillis = this.readTimeoutMillis,
    startTLSEnabled = this.startTLSEnabled,
    authEnabled = this.authEnabled,
    user = this.user,
    password = this.password
  )

  /**
    * The SMTP server to connect to.
    */
  def host: String = loadedConfig.flatMap(c => opt(c.getString("smtp.host"))).getOrElse("localhost")

  /**
    * The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25.
    */
  def port: Int = loadedConfig.flatMap(c => opt(c.getInt("smtp.port"))).getOrElse(587) // default: smtps

  /**
    * Socket connection timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.
    */
  def connectTimeoutMillis: Int = loadedConfig.flatMap(c => opt(c.getInt("smtp.connectTimeoutMillis"))).getOrElse(3000)

  /**
    * Socket read timeout value in milliseconds. This timeout is implemented by java.net.Socket. Default is infinite timeout.
    */
  def readTimeoutMillis: Int = loadedConfig.flatMap(c => opt(c.getInt("smtp.readTimeoutMillis"))).getOrElse(10000)

  /**
    * If true, enables the use of the STARTTLS command (if supported by the server)
    * to switch the connection to a TLS-protected connection before issuing any login commands.
    * Note that an appropriate trust store must configured
    * so that the client will trust the server's certificate. Defaults to false.
    */
  def startTLSEnabled: Boolean = loadedConfig.flatMap(c => opt(c.getBoolean("smtp.starttls.enabled"))).getOrElse(true)

  /**
    * If true, attempt to authenticate the user using the AUTH command. Defaults to false.
    */
  def authEnabled: Boolean = loadedConfig.flatMap(c => opt(c.getBoolean("smtp.auth.enabled"))).getOrElse(false)

  /**
    * Default user name for SMTP authentication if exists.
    */
  def user: Option[String] = loadedConfig.flatMap(c => opt(c.getString("smtp.auth.user")))

  /**
    * Default passsword for SMTP authentication if exists.
    */
  def password: Option[String] = loadedConfig.flatMap(c => opt(c.getString("smtp.auth.password")))

  /**
    * Password authenticator if exists.
    */
  def passwordAuthenticator: Option[Authenticator] = (user, password) match {
    case (Some(u), Some(p)) =>
      Some(new Authenticator {
        override def getPasswordAuthentication = {
          new PasswordAuthentication(u, p)
        }
      })
    case _ => None
  }

}
