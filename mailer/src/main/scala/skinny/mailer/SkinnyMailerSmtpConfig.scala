package skinny.mailer

import skinny.SkinnyEnv

object SkinnyMailerSmtpConfig {

  def default: SkinnyMailerSmtpConfig = SkinnyMailerSmtpConfig()

  def apply(name: String = "default", env: String = SkinnyEnv.getOrElse("development")): SkinnyMailerSmtpConfig = {
    val (n, e) = (name, env)
    new SkinnyMailerSmtpConfigApi {
      override def name = n
      override def skinnyEnv = e
    }.toCaseClass
  }

}

case class SkinnyMailerSmtpConfig(
  override val host: String,
  override val port: Int,
  override val connectTimeoutMillis: Int,
  override val readTimeoutMillis: Int,
  override val startTLSEnabled: Boolean,
  override val authEnabled: Boolean,
  override val user: Option[String],
  override val password: Option[String]
)
    extends SkinnyMailerSmtpConfigApi