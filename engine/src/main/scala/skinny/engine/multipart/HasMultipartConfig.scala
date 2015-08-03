package skinny.engine.multipart

import javax.servlet.ServletContext

import org.slf4j.LoggerFactory
import skinny.engine.Initializable

/**
 * Presents that multipart config has been activated.
 */
trait HasMultipartConfig extends Initializable { self: { def servletContext: ServletContext } =>

  import HasMultipartConfig._

  private[this] val logger = LoggerFactory.getLogger(getClass)

  private[this] def multipartConfigFromContext: Option[MultipartConfig] = {
    // hack to support the tests without changes
    providedConfig orElse {
      try {
        (Option(servletContext)
          .flatMap(sc => Option(sc.getAttribute(MultipartConfigKey)))
          .filterNot(_ == null)
          .map(_.asInstanceOf[MultipartConfig]))
      } catch {
        case _: NullPointerException => Some(DefaultMultipartConfig)
      }

    }
  }

  def multipartConfig: MultipartConfig = try {
    multipartConfigFromContext getOrElse DefaultMultipartConfig
  } catch {
    case e: Throwable =>
      logger.error("Couldn't get the multipart config from the servlet context because " + e.getMessage, e)
      DefaultMultipartConfig
  }

  private[this] var providedConfig: Option[MultipartConfig] = None

  abstract override def initialize(config: ConfigT) {
    super.initialize(config)
    providedConfig.foreach(_.apply(config.context))
  }

  def configureMultipartHandling(config: MultipartConfig): Unit = {
    providedConfig = Some(config)
  }

}

object HasMultipartConfig {

  val DefaultMultipartConfig = MultipartConfig()

  val MultipartConfigKey = "skinny.engine.MultipartConfigKey"

}
