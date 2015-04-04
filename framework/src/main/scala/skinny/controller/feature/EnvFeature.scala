package skinny.controller.feature

import skinny.SkinnyEnv
import org.scalatra.ScalatraBase

/**
 * SkinnyEnv support.
 */
trait EnvFeature extends ScalatraBase {

  /**
   * Env string value from "skinny.env", "org.scalatra.environment" or default value "development".
   *
   * @return env string such as "production"
   */
  // Skinny is unified lower case. Therefore, convert to lower case.
  def skinnyEnv: Option[String] = SkinnyEnv.get().orElse(Option(environment.toLowerCase))

  /**
   * Predicates current env is "development" or "dev".
   *
   * @return true/false
   */
  def isDevelopment(): Boolean = SkinnyEnv.isDevelopment(skinnyEnv)

  /**
   * Predicates current env is "test".
   *
   * @return true/false
   */
  def isTest(): Boolean = SkinnyEnv.isTest(skinnyEnv)

  /**
   * Predicates current env is "staging" or "qa".
   *
   * @return true/false
   */
  def isStaging(): Boolean = SkinnyEnv.isStaging(skinnyEnv)

  /**
   * Predicates current env is "production" or "prod".
   *
   * @return true/false
   */
  def isProduction(): Boolean = SkinnyEnv.isProduction(skinnyEnv)

  /**
   * Replaces Scalatra's development mode with SkinnyEnv.
   */
  override def isDevelopmentMode = isDevelopment()

}
