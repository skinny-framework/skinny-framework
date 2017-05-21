package skinny.view.freemarker

import scala.language.existentials
import scala.language.reflectiveCalls

import freemarker.template._
import freemarker.cache._
import javax.servlet.ServletContext
import java.io.File
import java.net.{ MalformedURLException, URL }
import freemarker.template.utility.StringUtil
import skinny.SkinnyEnv

/**
  * FreeMarker configuration factory.
  */
object FreeMarkerConfig {

  /**
    * Returns default configuration from ServletContext.
    *
    * @param ctx servlet context
    * @return configuration
    */
  def defaultWithServletContext(ctx: ServletContext, sbtProjectPath: Option[String] = None): Configuration = {
    val config = new Configuration {
      private[this] var _loaders: Seq[TemplateLoader] = Nil
      def addLoader(loader: TemplateLoader) = {
        _loaders ++= Seq(loader)
        setTemplateLoader(new MultiTemplateLoader(_loaders.toArray))
        this
      }
      def setLoaders(ldrs: TemplateLoader*): this.type = {
        _loaders = ldrs.toList
        setTemplateLoader(new MultiTemplateLoader(_loaders.toArray))
        this
      }
    }
    config.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER)
    config.setDefaultEncoding("UTF-8")
    config.addLoader(new SbtMultiProjectsSupportedWebappTemplateLoader(ctx, sbtProjectPath, "/WEB-INF/views/"))
    config.setObjectWrapper(new ScalaObjectWrapper())
    config
  }

  class SbtMultiProjectsSupportedWebappTemplateLoader(servletContext: ServletContext,
                                                      sbtProjectPath: Option[String] = None,
                                                      basePath: String)
      extends WebappTemplateLoader(servletContext, basePath) {

    override def findTemplateSource(name: String): AnyRef = {
      Option {
        // super
        super.findTemplateSource(name)
      }.getOrElse {
        // only for testing with sbt multiple projects
        if (SkinnyEnv.isTest()) {
          val rootPath    = new File("").getAbsolutePath
          val projectPath = rootPath + "/" + sbtProjectPath.getOrElse("")
          try {
            Option(servletContext.getRealPath(basePath + name))
              .map { path =>
                path.replaceFirst(rootPath, projectPath)
              }
              .map { path =>
                val f = new File(path)
                if (f.isFile && f.canRead) f
                else null
              }
              .orNull[File]
          } catch {
            case e: SecurityException => null
          }
        } else null
      }
    }
  }

}
