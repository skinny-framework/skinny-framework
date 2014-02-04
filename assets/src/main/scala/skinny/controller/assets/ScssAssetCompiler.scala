package skinny.controller.assets

import skinny._, assets._
import javax.servlet.ServletContext
import java.io.File

/**
 * Scss
 */
object ScssAssetCompiler extends AssetCompiler {
  private[this] val compiler = SassCompiler

  def dir(basePath: String) = throw new UnsupportedOperationException
  def extension = "scss"
  def compile(path: String, source: String) = compiler.compile(path, source)

  override def findClassPathResource(basePath: String, path: String): Option[ClassPathResource] = {
    ClassPathResourceLoader.getClassPathResource(s"${basePath}/scss/${path}.${extension}").orElse(
      ClassPathResourceLoader.getClassPathResource(s"${basePath}/sass/${path}.${extension}"))
  }

  override def findRealFile(servletContext: ServletContext, basePath: String, path: String): File = {
    val inScssDir = new File(servletContext.getRealPath(s"${basePath}/scss/${path}.${extension}"))
    if (inScssDir.exists) inScssDir
    else new File(servletContext.getRealPath(s"${basePath}/sass/${path}.${extension}"))
  }

}
