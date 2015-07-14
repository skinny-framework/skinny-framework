package skinny.engine.response

import java.io.{ Flushable, OutputStreamWriter, PrintWriter }
import java.nio.charset.Charset
import javax.servlet.http.{ HttpServletResponse, HttpServletResponseWrapper }

import skinny.engine.ContentEncoding

import scala.util.Try

/** Encodes any output written to a servlet response. */
private[skinny] class EncodedServletResponse(
    res: HttpServletResponse,
    enc: ContentEncoding) extends HttpServletResponseWrapper(res) {

  // Object to flush when complete, if any.
  // Note that while this is essentially a mutable shared state, it's not really an issue here - or rather, if multiple
  // threads are accessing your output stream at the same time, you have other, more important issues to deal with.
  private var toFlush: Option[Flushable] = None

  override lazy val getOutputStream: EncodedOutputStream = {
    val raw = super.getOutputStream
    val out = new EncodedOutputStream(enc.encode(raw), raw)

    addHeader("Content-Encoding", enc.name)
    toFlush = Some(out)
    out
  }

  override lazy val getWriter: PrintWriter = {
    val writer = new PrintWriter(new OutputStreamWriter(getOutputStream, getCharset))
    toFlush = Some(writer)
    writer
  }

  /** Returns the charset with which to encode the response. */
  private def getCharset: Charset = (for {
    name <- Option(getCharacterEncoding)
    charset <- Try(Charset.forName(name)).toOption
  } yield charset).getOrElse {
    // The charset is either not known or not supported, defaults to ISO 8859 1, as per RFC and servlet documentation.
    setCharacterEncoding("ISO-8859-1")
    Charset.forName("ISO-8859-1")
  }

  /** Ensures that whatever byte- or char-stream we have open is properly flushed. */
  override def flushBuffer(): Unit = {
    toFlush.foreach(_.flush())
    super.flushBuffer()
  }

  // Encoded responses do not have a content length.
  override def setContentLength(i: Int) = {}
  override def setContentLengthLong(len: Long): Unit = {}

}
