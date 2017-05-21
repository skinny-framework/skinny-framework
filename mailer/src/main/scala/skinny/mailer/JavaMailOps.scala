package skinny.mailer

import javax.mail._
import javax.mail.internet.MimeMessage
import scala.collection.JavaConverters._
import skinny.logging.LoggerProvider

/**
  * Operations about javax.mail APIs.
  */
object JavaMailOps extends LoggerProvider {

  def loggingTransport(session: Session): Transport = {
    new Transport(session, null) {
      def sendMessage(msg: Message, addresses: Array[Address]): Unit = {
        val mimeMsg = msg.asInstanceOf[MimeMessage]
        val content = try mimeMsg.getContent
        catch { case e: MessagingException => "" }
        logger.info {
          s"""
           |
           |##### SkinnyMailer Logging Transport #####
           |
           |${mimeMsg.getAllHeaderLines.asScala.mkString("\n")}
           |
           |${content}
           |""".stripMargin
        }
      }
    }
  }

  /**
    * Returns transport from session.
    */
  def transport(session: Session, auth: Option[SmtpAuthentication], protocol: String): Transport = {
    if (protocol == "logging") {
      loggingTransport(session)
    } else {
      val t = session.getTransport(protocol)
      auth.map(a => t.connect(a.user, a.password)).getOrElse(t.connect())
      t
    }
  }

}
