package skinny.mailer

import skinny.SkinnyEnv
import com.typesafe.config.{ Config, ConfigFactory }
import grizzled.slf4j.Logging

/**
 * Basic trait for SkinnyMailer configuration.
 */
trait SkinnyMailerConfigBase extends Logging {

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
    try Option(ConfigFactory.load().getConfig(s"${skinnyEnv}.mailer.${name}"))
    catch {
      case e: Exception =>
        logger.warn(s"Failed to load configuration for SkinnyMailer because ${e.getMessage}")
        None
    }
  }

}
