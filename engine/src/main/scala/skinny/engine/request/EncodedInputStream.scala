package skinny.engine.request

import java.io.InputStream
import javax.servlet.{ ReadListener, ServletInputStream }

private[skinny] class EncodedInputStream(
    encoded: InputStream,
    raw: ServletInputStream) extends ServletInputStream {

  override def isFinished: Boolean = raw.isFinished
  override def isReady: Boolean = raw.isReady
  override def setReadListener(readListener: ReadListener): Unit = raw.setReadListener(readListener)

  override def read(): Int = encoded.read()
  override def read(b: Array[Byte]): Int = read(b, 0, b.length)
  override def read(b: Array[Byte], off: Int, len: Int) = encoded.read(b, off, len)

}
