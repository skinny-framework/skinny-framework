package skinny.view.velocity

import java.io.StringWriter
import java.util.UUID

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.StringResourceLoader

trait VelocityTestSupport {

  protected def eval(templateAsString: String, params: (String, Any)*): String = {
    val velocity = new VelocityEngine {
      addProperty(RuntimeConstants.UBERSPECT_CLASSNAME, classOf[ScalaUberspect].getName)
      addProperty(RuntimeConstants.RESOURCE_LOADER, "string")
      addProperty("string.resource.loader.class", classOf[StringResourceLoader].getName)
    }
    velocity.init()

    val templateName = s"${UUID.randomUUID}.vm"
    classOf[StringResourceLoader] synchronized {
      StringResourceLoader.getRepository.putStringResource(templateName, templateAsString)
    }
    val context = new VelocityContext
    params.foreach { case (k, v) => context.put(k, v) }

    val template = velocity.getTemplate(templateName)
    val writer = new StringWriter
    template.merge(context, writer)
    writer.toString
  }

}
