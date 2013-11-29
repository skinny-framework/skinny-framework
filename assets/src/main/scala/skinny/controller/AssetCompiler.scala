package skinny.controller

import skinny._
import java.io.File
import javax.servlet.ServletContext

trait AssetCompiler {

  def dir(basePath: String): String

  def extension: String

  def compile(source: String): String

  def findClassPathResource(basePath: String, path: String): Option[ClassPathResource] = {
    ClassPathResourceLoader.getClassPathResource(s"${dir(basePath)}/${path}.${extension}")
  }

  def findRealFile(servletContext: ServletContext, basePath: String, path: String): File = {
    new File(servletContext.getRealPath(s"${dir(basePath)}/${path}.${extension}"))
  }

}
