package skinny.controller.feature

import javax.servlet.ServletOutputStream

import skinny.engine.SkinnyEngineBase
import skinny.logging.Logging

/**
 * Chunked Response (Transfer-Encoding: chunked).
 */
trait ChunkedResponseFeature { self: SkinnyEngineBase with Logging =>

  def withOutputStream(f: ServletOutputStream => Unit): Unit = {
    val stream = response.getOutputStream
    try {
      f.apply(stream)
    } catch {
      case e: Exception if Option(e.getCause).isDefined =>
        val message = Option(e.getMessage).getOrElse(Option(e.getCause).map(_.getMessage).getOrElse(""))
        if (logger.isDebugEnabled) {
          logger.debug(s"Chunked response error (message: ${message})", e)
        } else {
          logger.info(s"Chunked response error (message: ${message})")
        }
        try stream.close() catch {
          case e: Exception =>
            logger.debug(s"Failed to close output stream because ${e.getMessage})", e)
        }
    }
  }

  def writeChunk(chunk: Array[Byte])(implicit stream: ServletOutputStream = response.getOutputStream): Unit = {
    Option(chunk).foreach { c =>
      stream.write(c)
      stream.flush()
    }
  }

}
