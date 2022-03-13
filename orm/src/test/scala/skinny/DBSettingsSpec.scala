package skinny

import org.scalatest.flatspec.AnyFlatSpec

class DBSettingsSpec extends AnyFlatSpec with DBSettingsInitializer {

  behavior of "DBSettings"

  it should "have #initialize" in {
    val originalValue = System.getProperty(SkinnyEnv.EnvKey)
    try {
      System.setProperty(SkinnyEnv.EnvKey, "test")
      initialize(false)
      initialize(true)
      destroy()
    } finally {
      if (originalValue != null) {
        System.setProperty(SkinnyEnv.EnvKey, originalValue)
      }
    }
  }

}
