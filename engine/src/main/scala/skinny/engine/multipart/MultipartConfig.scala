package skinny.engine.multipart

import javax.servlet.{ MultipartConfigElement, ServletContext }

import skinny.engine.context.MountConfig

case class MultipartConfig(
    location: Option[String] = None,
    maxFileSize: Option[Long] = None,
    maxRequestSize: Option[Long] = None,
    fileSizeThreshold: Option[Int] = None) extends MountConfig {

  def toMultipartConfigElement: MultipartConfigElement = {
    new MultipartConfigElement(
      location.getOrElse(""),
      maxFileSize.getOrElse(-1),
      maxRequestSize.getOrElse(-1),
      fileSizeThreshold.getOrElse(0))
  }

  def apply(ctx: ServletContext): Unit = {
    ctx.setAttribute(HasMultipartConfig.MultipartConfigKey, this)
  }

}
