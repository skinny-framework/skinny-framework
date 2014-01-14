package skinny.mailer.feature

import skinny.mailer.{ SkinnyMessage, SkinnyMailerBase }
import javax.mail.{ Session, Transport }

/**
 * Provides SkinnyMessage builder.
 */
trait MessageBuilderFeature extends SkinnyMailerBase {

  self: ConfigFeature with SmtpConfigFeature with ExtraConfigFeature with JavaMailSessionFeature =>

  def from(from: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(from = from))

  def to(to: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(to = to))

  def subject(subject: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(subject = subject))

  def body(body: String)(implicit s: Session = session): SkinnyMessageBuilder = SkinnyMessageBuilder(mail(body = body))

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

    def to(to: String): SkinnyMessageBuilder = {
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

    def bcc(bcc: String): SkinnyMessageBuilder = {
      message.bcc = bcc
      this
    }
    def bcc(bcc: String*): SkinnyMessageBuilder = {
      message.bcc = bcc
      this
    }

    def cc(cc: String): SkinnyMessageBuilder = {
      message.cc = cc
      this
    }
    def cc(cc: String*): SkinnyMessageBuilder = {
      message.cc = cc
      this
    }

    // TODO validate state

    def deliver()(implicit t: Transport = message.connect()): Unit = message.deliver()

    def deliver(t: Transport, keepConnection: Boolean): Unit = message.deliver(t, keepConnection)
  }

}
