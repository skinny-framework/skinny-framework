package skinny

import skinny.util.TypesafeConfigReader
import com.typesafe.config.ConfigValue

/**
 * Skinny's configuration value loader.
 */
trait SkinnyConfig {

  def toConfigPathWithSkinnyEnv(path: String): String = SkinnyEnv.getOrElse(SkinnyEnv.Development) + "." + path

  def booleanConfigValue(path: String): Option[Boolean] = TypesafeConfigReader.boolean(toConfigPathWithSkinnyEnv(path))
  def booleanSeqConfigValue(path: String): Option[Seq[Boolean]] = TypesafeConfigReader.booleanSeq(toConfigPathWithSkinnyEnv(path))

  def doubleConfigValue(path: String): Option[Double] = TypesafeConfigReader.double(toConfigPathWithSkinnyEnv(path))
  def doubleSeqConfigValue(path: String): Option[Seq[Double]] = TypesafeConfigReader.doubleSeq(toConfigPathWithSkinnyEnv(path))

  def intConfigValue(path: String): Option[Int] = TypesafeConfigReader.int(toConfigPathWithSkinnyEnv(path))
  def intSeqConfigValue(path: String): Option[Seq[Int]] = TypesafeConfigReader.intSeq(toConfigPathWithSkinnyEnv(path))

  def longConfigValue(path: String): Option[Long] = TypesafeConfigReader.long(toConfigPathWithSkinnyEnv(path))
  def longSeqConfigValue(path: String): Option[Seq[Long]] = TypesafeConfigReader.longSeq(toConfigPathWithSkinnyEnv(path))

  def stringConfigValue(path: String): Option[String] = TypesafeConfigReader.string(toConfigPathWithSkinnyEnv(path))
  def stringSeqConfigValue(path: String): Option[Seq[String]] = TypesafeConfigReader.stringSeq(toConfigPathWithSkinnyEnv(path))

  def getConfigValue(path: String): Option[ConfigValue] = TypesafeConfigReader.get(toConfigPathWithSkinnyEnv(path))

}
