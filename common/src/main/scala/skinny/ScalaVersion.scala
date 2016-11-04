package skinny

object ScalaVersion {

  lazy val versionNumberString = scala.util.Properties.versionNumberString

  lazy val middleVersion: Int = versionNumberString.split("\\.")(1).toInt

  lazy val is_2_10: Boolean = middleVersion == 10
  lazy val is_2_11: Boolean = middleVersion == 11
  lazy val is_2_12: Boolean = middleVersion == 12

}
