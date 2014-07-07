package skinny

import skinny.util.TypesafeConfigReader
import com.typesafe.config.ConfigValue

object SkinnyConfig extends SkinnyConfig

/**
 * Skinny's configuration value loader.
 */
trait SkinnyConfig {

  private def env: String = SkinnyEnv.getOrElse(SkinnyEnv.Development)

  def booleanConfigValue(path: String): Option[Boolean] = TypesafeConfigReader.boolean(env, path)
  def booleanSeqConfigValue(path: String): Option[Seq[Boolean]] = TypesafeConfigReader.booleanSeq(env, path)

  def doubleConfigValue(path: String): Option[Double] = TypesafeConfigReader.double(env, path)
  def doubleSeqConfigValue(path: String): Option[Seq[Double]] = TypesafeConfigReader.doubleSeq(env, path)

  def intConfigValue(path: String): Option[Int] = TypesafeConfigReader.int(env, path)
  def intSeqConfigValue(path: String): Option[Seq[Int]] = TypesafeConfigReader.intSeq(env, path)

  def longConfigValue(path: String): Option[Long] = TypesafeConfigReader.long(env, path)
  def longSeqConfigValue(path: String): Option[Seq[Long]] = TypesafeConfigReader.longSeq(env, path)

  def stringConfigValue(path: String): Option[String] = TypesafeConfigReader.string(env, path)
  def stringSeqConfigValue(path: String): Option[Seq[String]] = TypesafeConfigReader.stringSeq(env, path)

  def getConfigValue(path: String): Option[ConfigValue] = TypesafeConfigReader.get(env, path)

}
