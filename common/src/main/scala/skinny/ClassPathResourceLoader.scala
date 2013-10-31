package skinny

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
  def getClassPathResource(path: String): Option[ClassPathResource] = {
    val relativePath = path.stripPrefix("/")
    getResourceFromClassLoader(Thread.currentThread.getContextClassLoader, relativePath)
      .orElse(getResourceFromClassLoader(getClass.getClassLoader, relativePath))
  }

  private[this] def getResourceFromClassLoader(classLoader: ClassLoader, path: String): Option[ClassPathResource] = {
    try {
      val resource = classLoader.getResource(path)
      val conn = resource.openConnection
      val lastModified = conn.getLastModified
      Some(ClassPathResource(conn.getInputStream, lastModified))
    } catch { case e: Exception =>
      None
    }
  }

}
