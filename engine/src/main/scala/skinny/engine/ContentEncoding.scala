package skinny.engine

import java.io._
import java.util.zip.{ DeflaterOutputStream, GZIPInputStream, GZIPOutputStream, InflaterInputStream }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.request.DecodedServletRequest
import skinny.engine.response.EncodedServletResponse

/**
 * Content encoding
 *
 * Represents an HTTP content encoding.
 */
trait ContentEncoding {

  /** Name of the encoding, as used in the `Content-Encoding` and `Accept-Encoding` headers. */
  def name: String

  /** Wraps the specified output stream into an encoding one. */
  def encode(out: OutputStream): OutputStream

  /** Wraps the specified input stream into a decoding one. */
  def decode(in: InputStream): InputStream

  override def toString = name

  def apply(response: HttpServletResponse): HttpServletResponse = new EncodedServletResponse(response, this)

  def apply(request: HttpServletRequest): HttpServletRequest = new DecodedServletRequest(request, this)

}

object ContentEncoding {

  private def create(id: String, e: OutputStream => OutputStream, d: InputStream => InputStream): ContentEncoding = {
    new ContentEncoding {
      override def name: String = id
      override def encode(out: OutputStream): OutputStream = e(out)
      override def decode(in: InputStream): InputStream = d(in)
    }
  }

  val GZip: ContentEncoding = {
    create("gzip", out => new GZIPOutputStream(out), in => new GZIPInputStream(in))
  }

  val Deflate: ContentEncoding = {
    create("deflate", out => new DeflaterOutputStream(out), in => new InflaterInputStream(in))
  }

  def forName(name: String): Option[ContentEncoding] = name.toLowerCase match {
    case "gzip" => Some(GZip)
    case "deflate" => Some(Deflate)
    case _ => None
  }
}
