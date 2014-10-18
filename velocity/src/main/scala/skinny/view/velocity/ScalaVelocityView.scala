package skinny.view.velocity

import java.io.File
import javax.servlet.ServletContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.FileResourceLoader
import org.apache.velocity.tools.view.{ JeeConfig, VelocityView, WebappResourceLoader }
import scala.collection.JavaConverters._
import skinny.SkinnyEnv

/**
 * VelocityView extension for Scala
 */
class ScalaVelocityView(
    ctx: ServletContext, sbtProjectPath: Option[String]) extends VelocityView(ctx) {

  override protected def configure(config: JeeConfig, velocity: VelocityEngine): Unit = {
    super.configure(config, velocity)

    val propName = RuntimeConstants.UBERSPECT_CLASSNAME
    velocity.getProperty(propName) match {
      case null =>
        velocity.setProperty(propName, classOf[ScalaUberspect].getName)

      case name: String if name == classOf[ScalaUberspect].getName => // NOOP

      case name: String =>
        // prepends ScalaUberspect to VelocityEngine
        velocity.setProperty(propName, classOf[ScalaUberspect].getName)
        velocity.addProperty(propName, name)

      case uberspectClassNames: java.util.Vector[_] if uberspectClassNames.asScala.contains(classOf[ScalaUberspect].getName) => // NOOP

      case uberspectClassNames: java.util.Vector[_] =>
        // prepends ScalaUberspect to VelocityEngine
        velocity.setProperty(propName, classOf[ScalaUberspect].getName)
        uberspectClassNames.asScala.foreach(name => velocity.addProperty(propName, name))
    }

    // Add ResourceLoader for views
    val skinyWebappLoaderName = "skinny-webapp"
    val viewLocation = "/WEB-INF/views/"
    velocity.addProperty(RuntimeConstants.RESOURCE_LOADER, skinyWebappLoaderName)
    velocity.addProperty(s"${skinyWebappLoaderName}.resource.loader.class", classOf[WebappResourceLoader].getName)
    velocity.addProperty(s"${skinyWebappLoaderName}.resource.loader.path", viewLocation)

    if ("UTF-8" != velocity.getProperty(RuntimeConstants.INPUT_ENCODING)) {
      // template encoding, force UTF-8
      velocity.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8")
    }

    if (SkinnyEnv.isTest()) {
      val skinnyTestFileLoaderName = "skinny-test-file"
      val rootPath = new File("").getAbsolutePath
      val projectPath = rootPath + "/" + sbtProjectPath.getOrElse("")
      velocity.addProperty(RuntimeConstants.RESOURCE_LOADER, skinnyTestFileLoaderName)
      velocity.addProperty(s"${skinnyTestFileLoaderName}.resource.loader.class", classOf[FileResourceLoader].getName)
      velocity.addProperty(s"${skinnyTestFileLoaderName}.resource.loader.path", ctx.getRealPath(viewLocation).replaceFirst(rootPath, projectPath))
    }
  }

}
