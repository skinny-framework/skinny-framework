package skinny.test

import java.io.ByteArrayOutputStream
import javax.servlet.{ ServletOutputStream, WriteListener }

class MockServletOutputStream extends ServletOutputStream {

  private[this] val byteArrayOutputStream = new ByteArrayOutputStream()

  override def write(i: Int) = byteArrayOutputStream.write(i)

  override def isReady: Boolean = true

  override def setWriteListener(writeListener: WriteListener): Unit = ???

  override def toString = byteArrayOutputStream.toString

  def toString(charset: String) = byteArrayOutputStream.toString(charset)

  def toByteArray = byteArrayOutputStream.toByteArray
}
