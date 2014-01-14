package skinny.mailer

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import javax.mail.Session
import org.joda.time.DateTime
import skinny.mailer.implicits.SkinnyMessageImplicits

class SkinnyMessageSpec extends FlatSpec with ShouldMatchers with SkinnyMessageImplicits {
  behavior of "SkinnyMessage"

  val session = Session.getInstance(new java.util.Properties())

  it should "properties" in {
    val msg = new SkinnyMessage(session)
    msg.sentDate = new DateTime(2013, 12, 1, 0, 0).toDate
    msg.sentDate should be(new DateTime(2013, 12, 1, 0, 0).toDate)

    msg.subject = "Subject1"
    msg.subject should be("Subject1")

    msg.subject = ("Subject2", "UTF-8")
    msg.subject should be("Subject2")

    msg.sender = "sender@skinny.org"
    msg.sender.toString should be("sender@skinny.org")

    msg.replyTo = "rep1@skinny.org,rep2@skinny.org"
    msg.replyTo(0).toString should be("rep1@skinny.org")
    msg.replyTo(1).toString should be("rep2@skinny.org")

    msg.header = ("header1", "h1")
    msg.header = ("header2", "h2")
    msg.header = Map("header3" -> "h3", "header4" -> "h4")
    msg.header("header1")(0) should be("h1")
    msg.header("header2")(0) should be("h2")
    msg.header("header3")(0) should be("h3")
    msg.header("header4")(0) should be("h4")

    msg.from = "from@skinny.org"
    msg.from(0).toString should be("from@skinny.org")

    msg.recipients = (javax.mail.Message.RecipientType.TO -> "to1@skinny.org,to2@skinny.org")
    msg.recipients(javax.mail.Message.RecipientType.TO)(0).toString should be("to1@skinny.org")
    msg.recipients(javax.mail.Message.RecipientType.TO)(1).toString should be("to2@skinny.org")

    msg.filename = "filename"
    msg.filename should be("filename")

    // datahandler

    msg.contentLanguage = Array("ja", "en")
    msg.contentLanguage(0) should be("ja")
    msg.contentLanguage(1) should be("en")

    msg.contentMD5 = "".hashCode.toString
    msg.contentMD5 should be("".hashCode.toString)

    msg.contentID = "contentID"
    msg.contentID should be("contentID")

    msg.to = "to3@skinny.org,to4@skinny.org"
    msg.to.length should be(2)
    msg.to(0).toString should be("to3@skinny.org")
    msg.to(1).toString should be("to4@skinny.org")

    msg.cc = "cc1@skinny.org,cc2@skinny.org"
    msg.cc.length should be(2)
    msg.cc(0).toString should be("cc1@skinny.org")
    msg.cc(1).toString should be("cc2@skinny.org")

    msg.disposition = "attachment"
    msg.disposition should be("attachment")

    msg.description = "description"
    msg.description should be("description")

    msg.mimeVersion = "1.0"
    msg.mimeVersion should be("1.0")
  }

  it should "not override content" in {
    val msg = new SkinnyMessage(session)
    msg.attachment ++= ("dummy.txt", getClass.getResource("/dummy.txt").toURI.getPath)

    msg.attachments.length should be(1)
    msg.text = "hello"
    msg.attachments.length should be(1)
    msg.text should be("hello")
  }
}
