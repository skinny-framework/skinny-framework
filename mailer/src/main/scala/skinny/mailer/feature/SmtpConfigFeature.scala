package skinny.mailer.feature

import skinny.mailer.SkinnyMailerSmtpConfigApi

/**
  * Provides SkinnyMailerSmtpConfig
  */
trait SmtpConfigFeature { self: ConfigFeature =>

  /**
    * Returns loaded config on SMTP.
    *
    * @return config
    */
  def smtpConfig: SkinnyMailerSmtpConfigApi = self.config.smtp

}
