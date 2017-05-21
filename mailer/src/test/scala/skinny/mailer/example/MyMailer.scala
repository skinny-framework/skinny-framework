package skinny.mailer.example

import javax.mail.MessagingException
import skinny.mailer.{ SkinnyMailer, SkinnyMessage }

class MyMailer extends SkinnyMailer {

  def sendMessage(toAddress: String) = {
    from("from@example.com")
      .to(toAddress)
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
      }
      .deliver()
  }

  def sendMessage2(toAddress: String) = {
    from("from@example.com")
      .envelopeFrom("e-from@example.com")
      .to(toAddress)
      .subject("subject2")
      .body("body2")
      .deliver()
  }

  def sendOther = from("from@example.com").to("other@example.com").deliver()

  def notSending: Either[MessagingException, SkinnyMessage] = Left(new MessagingException())

}
