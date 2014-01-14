package skinny.mailer.example

import javax.mail.MessagingException
import skinny.mailer.{ SkinnyMessage, SkinnyMailer }

class MyMailer extends SkinnyMailer {

  def sendMessage(toAddress: String) = {
    to(toAddress)
      .cc("cc@example.com", "cc2@example.com")
      .bcc("bcc@example.com")
      .subject("test subject 日本語")
      .body {
        s"""${toAddress} 様
        |
        |いつもご利用ありがとうございます。
        |〜〜をお知らせいたします。
        |
        |""".stripMargin
      }.deliver()
  }

  def sendMessage2(toAddress: String) = to(toAddress).subject("subject2").body("body2").deliver()

  def sendOther = to("other@example.com").deliver()

  def notSending: Either[MessagingException, SkinnyMessage] = Left(new MessagingException())

}
