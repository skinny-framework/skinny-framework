package skinny.assets

import java.io.InputStream

/**
 * Class path resource loader.
 */
object ClassPathResourceLoader {

  /**
   * Returns a resource as an InputStream if exists.
   *
   * @param path path
   * @return resource if exists
   */
  def getResourceAsStream(path: String): Option[InputStream] = {
    val relativePath = path.stripPrefix("/")
    Option(Thread.currentThread.getContextClassLoader.getResourceAsStream(relativePath))
      .orElse(Option(getClass.getClassLoader.getResourceAsStream(relativePath)))
  }

}
