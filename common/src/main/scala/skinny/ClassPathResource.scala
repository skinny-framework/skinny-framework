package skinny

import java.io.InputStream

/**
  * Resource loaded from class path.
  *
  * @param stream input stream
  * @param lastModified last modified millis
  */
case class ClassPathResource(stream: InputStream, lastModified: Long)
