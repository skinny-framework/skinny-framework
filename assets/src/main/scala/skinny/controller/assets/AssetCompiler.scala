package skinny.controller.assets

import skinny._
import java.io.File
import javax.servlet.ServletContext

/**
 * Asset compiler for AssetsController
 */
trait AssetCompiler {

  /**
   * Base directory. e.g. /WEB-INF/assets/coffee
   * @param basePath base path
   * @return base directory
   */
  def dir(basePath: String): String

  /**
   * Returns extension for this asset.
   * @return extension
   */
  def extension: String

  /**
   * Compiles source code to js/css code.
   * @param source code
   * @return js/css code
   */
  def compile(path: String, source: String): String

  /**
   * Finds class path resource.
   * @param basePath base path
   * @param path path
   * @return class path resource
   */
  def findClassPathResource(basePath: String, path: String): Option[ClassPathResource] = {
    ClassPathResourceLoader.getClassPathResource(s"${dir(basePath)}/${path}.${extension}")
  }

  /**
   * Finds real file from ServletContext.
   * @param servletContext servlet context
   * @param basePath base path
   * @param path path
   * @return real file
   */
  def findRealFile(servletContext: ServletContext, basePath: String, path: String): File = {
    new File(servletContext.getRealPath(s"${dir(basePath)}/${path}.${extension}"))
  }

}
