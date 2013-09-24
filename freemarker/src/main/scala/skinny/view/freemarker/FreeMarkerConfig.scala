package skinny.view.freemarker

import scala.language.existentials
import scala.language.reflectiveCalls

import freemarker.template._
import freemarker.cache._
import javax.servlet.ServletContext

object FreeMarkerConfig {

  def defaultWithServletContext(ctx: ServletContext): Configuration = {
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
    config.addLoader(new WebappTemplateLoader(ctx, "/WEB-INF/views"))
    config.setObjectWrapper(new ScalaObjectWrapper())
    config
  }

}
