package skinny.mailer

import skinny.SkinnyEnv

object SkinnyMailerConfig {

  def default: SkinnyMailerConfig = SkinnyMailerConfig()

  def apply(name: String = "default", env: String = SkinnyEnv.getOrElse("development")): SkinnyMailerConfig = {
    val (n, e) = (name, env)
    new SkinnyMailerConfigApi {
      override def name = n
      override def skinnyEnv = e
    }.toCaseClass
  }

}

case class SkinnyMailerConfig(
  override val debug: Boolean,
  override val mimeVersion: String,
  override val charset: String,
  override val contentType: String,
  override val defaultFrom: Option[String],
  override val transportProtocol: String,
  override val smtp: SkinnyMailerSmtpConfigApi
)
    extends SkinnyMailerConfigApi {

}

