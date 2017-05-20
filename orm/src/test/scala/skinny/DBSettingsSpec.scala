package skinny

import org.scalatest.FlatSpec

class DBSettingsSpec extends FlatSpec with DBSettingsInitializer {

  behavior of "DBSettings"

  it should "have #initialize" in {
    val originalValue = System.getProperty(SkinnyEnv.EnvKey)
    try {
      System.setProperty(SkinnyEnv.EnvKey, "test")
      initialize(false)
      initialize(true)
      destroy()
    } finally {
      System.setProperty(SkinnyEnv.EnvKey, originalValue)
    }
  }

}
