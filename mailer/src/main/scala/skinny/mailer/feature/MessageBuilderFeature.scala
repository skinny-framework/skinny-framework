package skinny.mailer.feature

import skinny.mailer.{ SkinnyMessage, SkinnyMailerBase }
import javax.mail.{ Session, Transport }

/**
 * Provides SkinnyMessage builder.
 */
trait MessageBuilderFeature extends SkinnyMailerBase {

  self: ConfigFeature with SmtpConfigFeature with ExtraConfigFeature with JavaMailSessionFeature =>

  def from(from: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(from = from))

  def to(to: String*)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(to = to))

  def subject(subject: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(subject = subject))

  def body(body: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(body = body))

  def htmlBody(body: String, charset: String = config.charset)(implicit s: Session = session): SkinnyMessageBuilder = {
    val msg = mail(body = body)
    msg.charset = charset
    msg.contentType = "text/html"
    msg.setText(body, charset, "html")
    SkinnyMessageBuilder(msg)
  }

  def bcc(bcc: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder({
    val m = mail()
    m.bcc = bcc
    m
  })

  def cc(cc: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder({
    val m = mail()
    m.cc = cc
    m
  })

  /**
   * SkinnyMessage builder.
   *
   * @param message underlying message (mutable)
   */
  case class SkinnyMessageBuilder(message: SkinnyMessage) {

    def from(from: String): SkinnyMessageBuilder = {
      message.from = from
      this
    }

    def to(to: String*): SkinnyMessageBuilder = {
      message.to = to
      this
    }

    def subject(subject: String): SkinnyMessageBuilder = {
      message.subject = subject
      this
    }

    def body(body: String): SkinnyMessageBuilder = {
      message.body = body
      this
    }

    def htmlBody(body: String, charset: String = config.charset): SkinnyMessageBuilder = {
      message.charset = charset
      message.contentType = "text/html"
      message.setText(body, charset, "html")
      this
    }

    def bcc(bcc: String*): SkinnyMessageBuilder = {
      message.bcc = bcc
      this
    }

    def cc(cc: String*): SkinnyMessageBuilder = {
      message.cc = cc
      this
    }

    def attachment(filename: String, content: AnyRef, mimeType: String): SkinnyMessageBuilder = {
      message.attachments ++= (filename, content, mimeType)
      this
    }

    def validate() = {
      // TODO NPE in sbt test (sbt mailer/test works)
      if (message != null && message.from != null && message.to != null) {
        if (message.from.isEmpty) throw new IllegalStateException("from address is absent")
        else if (message.to.isEmpty) throw new IllegalStateException("to addresses are empty")
      }
    }

    def deliver()(implicit t: Transport = message.connect()): Unit = {
      validate()
      message.deliver()
    }

    def deliver(t: Transport, keepConnection: Boolean): Unit = {
      validate()
      message.deliver(t, keepConnection)
    }
  }

}
