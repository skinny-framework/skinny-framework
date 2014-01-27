package controller

import skinny.mailer.SkinnyMailer

class MailController extends ApplicationController {
  protectFromForgery()

  override def scalateExtensions = List("jade")

  class MyMailer extends SkinnyMailer

  def index = {
    val mailer = new MyMailer
    mailer.from("seratch+from@gmail.com")
      .to("seratch+to@gmail.com")
      .cc("seratch+cc1@gmail.com", "seratch+cc2@gmail.com")
      .subject("Skinny Mailer example")
      .body {
        """Dear customer,
        |
        |Blah-blah-blah...
        |
        |Best Regards,
        |XXXX
      """.stripMargin
      }.deliver()

    render("/root/index")
  }

  def ssp = {
    val mailer = new MyMailer
    mailer.from("seratch+from@gmail.com")
      .to("seratch+to@gmail.com")
      .subject("ssp test")
      .htmlBody {
        render("/mail/example")
      }.attachment("memo.txt", "メモ", "text/plain; charset=utf-8")
      .deliver()
    "ok"
  }

}
