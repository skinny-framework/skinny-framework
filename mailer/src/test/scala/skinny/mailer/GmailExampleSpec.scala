package skinny.mailer

/**
 * GMail SMTP Example.
 */
class GmailExampleSpec {

  /**
   * Paste this code on the REPL!
   */
  def example {

    // https://support.google.com/accounts/answer/185833
    val yourGmail = "*****@gmail.com"
    val yourPassword = "*****"

    import skinny.mailer._

    // Prepare configuration for GMail SMTP
    val config = SkinnyMailerConfig.default.copy(
      debug = true,
      defaultFrom = Some(yourGmail),
      transportProtocol = "smtps",
      smtp = SkinnyMailerSmtpConfig().copy(
        host = "smtp.gmail.com",
        port = 465,
        authEnabled = true,
        user = Some(yourGmail),
        password = Some(yourPassword)
      )
    )

    // create a SkinnyMailer
    val GMail = SkinnyMailer(config)

    GMail.
      to(yourGmail).
      cc(yourGmail).
      subject("SkinnyMailer GMail Test").
      body {
        """You succeeded sending email via GMail SMTP server!
        |
        |Blah-blah-blah...
        |
      """.stripMargin
      }.deliver()
  }

}
