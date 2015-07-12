package skinny.mailer

import com.typesafe.config.Config
import skinny.SkinnyEnv
import skinny.logging.LoggerProvider
import skinny.util.TypesafeConfigReader

/**
 * Basic trait for SkinnyMailer configuration.
 */
trait SkinnyMailerConfigBase extends LoggerProvider {

  /**
   * Name for this configuration which will be use in the namespace "{env}.mailer.{name}".
   */
  def name: String = "default"

  /**
   * Skinny environment value.
   */
  def skinnyEnv: String = SkinnyEnv.get().getOrElse(SkinnyEnv.Development)

  /**
   * Loaded Typesafe Config object.
   */
  lazy val loadedConfig: Option[Config] = {
    try Option(TypesafeConfigReader.config(skinnyEnv).getConfig(s"mailer.${name}"))
    catch {
      case e: Exception =>
        logger.warn(s"Failed to load configuration for SkinnyMailer because ${e.getMessage}")
        None
    }
  }

  // scala.Try may return Some(null)
  protected def opt[A](op: => A): Option[A] = {
    try Option(op)
    catch {
      case e: Exception =>
        None
    }
  }

}
