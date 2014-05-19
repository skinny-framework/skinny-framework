package skinny.mailer

import org.scalatest._
import javax.mail.Session
import org.joda.time.DateTime
import skinny.mailer.implicits.SkinnyMailerImplicits

class SkinnyMessageSpec extends FlatSpec with Matchers with SkinnyMailerImplicits {
  behavior of "SkinnyMessage"

  val session = Session.getInstance(new java.util.Properties())

  it should "properties" in {
    val msg = new SkinnyMessage(session)
    msg.sentDate = new DateTime(2013, 12, 1, 0, 0)
    msg.sentDate.get should be(new DateTime(2013, 12, 1, 0, 0))

    msg.subject = "Subject1"
    msg.subject should be(Some("Subject1"))

    msg.subject = ("Subject2", "UTF-8")
    msg.subject should be(Some("Subject2"))

    msg.sender = "sender@skinny-framework.org"
    msg.sender.toString should be("sender@skinny-framework.org")

    msg.replyTo = "rep1@skinny-framework.org,rep2@skinny-framework.org"
    msg.replyTo(0).toString should be("rep1@skinny-framework.org")
    msg.replyTo(1).toString should be("rep2@skinny-framework.org")

    msg.header = "header1" -> "h1"
    msg.header = "header2" -> "h2"
    msg.header = Map("header3" -> "h3", "header4" -> "h4")
    msg.header("header1")(0) should be("h1")
    msg.header("header2")(0) should be("h2")
    msg.header("header3")(0) should be("h3")
    msg.header("header4")(0) should be("h4")

    msg.from = "from@skinny-framework.org"
    msg.from.map(_.toString) should be(Some("from@skinny-framework.org"))

    msg.recipients = (javax.mail.Message.RecipientType.TO -> "to1@skinny-framework.org,to2@skinny-framework.org")
    msg.recipients(javax.mail.Message.RecipientType.TO)(0).toString should be("to1@skinny-framework.org")
    msg.recipients(javax.mail.Message.RecipientType.TO)(1).toString should be("to2@skinny-framework.org")

    msg.filename = "filename"
    msg.filename should be("filename")

    // data handler

    msg.contentLanguage = Array("ja", "en")
    msg.contentLanguage(0) should be("ja")
    msg.contentLanguage(1) should be("en")

    msg.contentMD5 = "".hashCode.toString
    msg.contentMD5 should be("".hashCode.toString)

    msg.contentID = "contentID"
    msg.contentID should be("contentID")

    msg.to = "to3@skinny-framework.org,to4@skinny-framework.org"
    msg.to.length should be(2)
    msg.to(0).toString should be("to3@skinny-framework.org")
    msg.to(1).toString should be("to4@skinny-framework.org")

    msg.cc = "cc1@skinny-framework.org,cc2@skinny-framework.org"
    msg.cc.length should be(2)
    msg.cc(0).toString should be("cc1@skinny-framework.org")
    msg.cc(1).toString should be("cc2@skinny-framework.org")

    msg.cc = Seq("cc3@skinny-framework.org", "cc4@skinny-framework.org")
    msg.cc.length should be(2)
    msg.cc(0).toString should be("cc3@skinny-framework.org")
    msg.cc(1).toString should be("cc4@skinny-framework.org")

    msg.disposition = "attachment"
    msg.disposition should be("attachment")

    msg.description = "description"
    msg.description should be("description")

    msg.mimeVersion = "1.0"
    msg.mimeVersion should be("1.0")
  }

}
