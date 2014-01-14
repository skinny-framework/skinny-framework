package skinny.mailer

import org.fusesource.scalate.support.TemplateFinder
import org.fusesource.scalate.{ TemplateEngine }
import skinny.SkinnyEnv

/**
 * ScalateSkinnyMailerSupport creates and configures a template engine for mailer module and provides helper method.
 */
trait ScalateSkinnyMailerSupport {
  /**
   * The project root path.
   * ServletContext#getRealPath("/") is recommended.
   */
  lazy val baseDir = System.getProperty("user.dir")

  /**
   * The default path to search for templates.
   *
   */
  lazy val defaultTemplatePath: List[String] = List(
    "/WEB-INF/views/mailer",
    baseDir,
    (baseDir + "/WEB-INF/views/mailer").replaceAll("//", "/"),
    (baseDir + "/src/main/webapp/WEB-INF/views/mailer").replaceAll("//", "/")
  )

  /**
   * Creates the templateEngine.
   */
  protected def createTemplateEngine: TemplateEngine = {
    val engine = new TemplateEngine {
      override def isDevelopmentMode = SkinnyEnv.isDevelopment()
    }
    engine.templateDirectories :::= defaultTemplatePath
    engine
  }

  /**
   * Finds and renders a template,
   * returning the result.
   * @param ext an extension like "ssp"
   * @param path path The path of the template, passed to `findTemplate`
   * @param bindings bin
   * @param typ text or html
   * @return
   */
  protected def layoutTemplateAs(ext: String)(path: String, bindings: Map[String, Any], typ: MailExtensionType = TextPlain): String = {
    val templateEngine = createTemplateEngine
    val uri = findTemplate(templateEngine, path, ext, typ)
    val out = templateEngine.layout(uri, bindings)
    templateEngine.shutdown()
    out
  }

  /**
   * Finds a template for a path.  Delegates to a TemplateFinder.
   */
  protected def findTemplate(templateEngine: TemplateEngine, path: String, ext: String = "ssp", typ: MailExtensionType = TextPlain) = {
    val templatePath = s"${path}.${typ.ext}"
    val finder = new TemplateFinder(templateEngine) {
      override lazy val extensions = Set(ext)
    }
    finder.findTemplate(("/" + templatePath).replaceAll("//", "/")).getOrElse(s"${templatePath}.${ext}")
  }

  /**
   * Convenience method for `layoutTemplateAs("ssp")`.
   */
  def ssp(path: String, bindings: Map[String, Any], typ: MailExtensionType = TextPlain) = layoutTemplateAs("ssp")(path, bindings, typ)
  /**
   * Convenience method for `layoutTemplateAs("scaml")`.
   */
  def scaml(path: String, bindings: Map[String, Any], typ: MailExtensionType = TextPlain) = layoutTemplateAs("scaml")(path, bindings, typ)
  /**
   * Convenience method for `layoutTemplateAs("jade")`.
   */
  def jade(path: String, bindings: Map[String, Any], typ: MailExtensionType = TextPlain) = layoutTemplateAs("jade")(path, bindings, typ)
  /**
   * Convenience method for `layoutTemplateAs("mustache")`.
   */
  def mustache(path: String, bindings: Map[String, Any], typ: MailExtensionType = TextPlain) = layoutTemplateAs("mustache")(path, bindings, typ)
}

abstract sealed class MailExtensionType {
  val ext: String
}

/**
 * XXX.text.XXX will be used
 */
case object TextPlain extends MailExtensionType {
  override val ext = "text"
}

/**
 * XXX.html.XXX will be used
 */
case object TextHtml extends MailExtensionType {
  override val ext = "html"
}
