package skinny.mailer.feature

import org.fusesource.scalate.support.TemplateFinder
import org.fusesource.scalate.TemplateEngine
import skinny.SkinnyEnv
import skinny.mailer.{ Text, BodyType }

/**
 * ScalateTemplateFeature creates and configures Scalate template engine
 * for mailer module and provides helper methods.
 */
trait ScalateTemplateFeature {

  // TODO baseDir

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
   * @param bodyType text or html
   * @return
   */
  protected def layoutTemplateAs(ext: String)(path: String, bindings: Map[String, Any], bodyType: BodyType = Text): String = {
    val templateEngine = createTemplateEngine
    val uri = findTemplate(templateEngine, path, ext, bodyType)
    val out = templateEngine.layout(uri, bindings)
    templateEngine.shutdown()
    out
  }

  /**
   * Finds a template for a path.  Delegates to a TemplateFinder.
   */
  protected def findTemplate(templateEngine: TemplateEngine, path: String, ext: String = "ssp", bodyType: BodyType = Text) = {
    val templatePath = s"${path}.${bodyType.extension}"
    val finder = new TemplateFinder(templateEngine) {
      override lazy val extensions = Set(ext)
    }
    finder.findTemplate(("/" + templatePath).replaceAll("//", "/")).getOrElse(s"${templatePath}.${ext}")
  }

  /**
   * Convenience method for `layoutTemplateAs("ssp")`.
   */
  def ssp(path: String, bindings: Map[String, Any], bodyType: BodyType = Text) = {
    layoutTemplateAs("ssp")(path, bindings, bodyType)
  }

  /**
   * Convenience method for `layoutTemplateAs("scaml")`.
   */
  def scaml(path: String, bindings: Map[String, Any], bodyType: BodyType = Text) = {
    layoutTemplateAs("scaml")(path, bindings, bodyType)
  }

  /**
   * Convenience method for `layoutTemplateAs("jade")`.
   */
  def jade(path: String, bindings: Map[String, Any], bodyType: BodyType = Text) = {
    layoutTemplateAs("jade")(path, bindings, bodyType)
  }

  /**
   * Convenience method for `layoutTemplateAs("mustache")`.
   */
  def mustache(path: String, bindings: Map[String, Any], bodyType: BodyType = Text) = {
    layoutTemplateAs("mustache")(path, bindings, bodyType)
  }

}
