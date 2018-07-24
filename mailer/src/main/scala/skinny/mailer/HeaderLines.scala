package skinny.mailer

import scala.collection.JavaConverters._

/**
  * Header lines of MIME message.
  */
case class HeaderLines(message: RichMimeMessage) {

  /**
    * Add a raw RFC 822 header-line.
    */
  def ++=(lines: String) = message.underlying.addHeaderLine(lines)

  /**
    * Add a raw RFC 822 header-line.
    */
  def ++=(lines: Iterable[String]) = lines.foreach(message.underlying.addHeaderLine)

  /**
    * Returns as a Seq value.
    */
  def toSeq: Seq[String] = {
    Option(message.underlying.getAllHeaderLines)
      .map(_.asScala.toIndexedSeq)
      .getOrElse(Seq.empty)
      .map(_.asInstanceOf[String])
      .toIndexedSeq
  }

}
