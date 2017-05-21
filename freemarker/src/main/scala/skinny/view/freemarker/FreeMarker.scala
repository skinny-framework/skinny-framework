package skinny.view.freemarker

import freemarker.template.Configuration

/**
  * FreeMarker renderer.
  *
  * @param config configuration
  */
case class FreeMarker(config: Configuration) {

  def render(path: String, values: Map[String, Any]): String = {
    val template = config.getTemplate(path)
    val result   = new java.io.StringWriter
    template.process(values, result)
    result.toString
  }
}
