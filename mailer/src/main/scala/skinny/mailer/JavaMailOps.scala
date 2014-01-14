package skinny.mailer

import javax.mail._

/**
 * Operations about javax.mail APIs.
 */
object JavaMailOps {

  /**
   * Returns transport from session.
   */
  def transport(session: Session, auth: Option[SmtpAuthentication], protocol: String): Transport = {
    val t = session.getTransport(protocol)
    auth.map(a => t.connect(a.user, a.password)).getOrElse(t.connect())
    t
  }

}
