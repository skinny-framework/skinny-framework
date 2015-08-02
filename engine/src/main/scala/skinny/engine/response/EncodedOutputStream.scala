package skinny.engine.response

import java.io.OutputStream
import javax.servlet.ServletOutputStream

/**
 * Encoded output stream in a Servlet response.
 *
 * Wraps the specified raw and servlet output streams into one servlet output stream.
 */
private[skinny] class EncodedOutputStream(
    out: OutputStream,
    orig: ServletOutputStream) extends ServletOutputStream {

  override def write(b: Int): Unit = out.write(b)
  override def write(b: Array[Byte]) = write(b, 0, b.length)
  override def write(b: Array[Byte], off: Int, len: Int): Unit = out.write(b, off, len)

  override def flush(): Unit = out.flush()
  override def close(): Unit = out.close()

}
