package skinny

import org.scalatest.FlatSpec

class DBSettingsSpec extends FlatSpec with DBSettings with DBSettingsInitializer {

  System.setProperty(SkinnyEnv.EnvKey, "test")

  behavior of "DBSettings"

  it should "have #initialize" in {
    initialize(false)
    initialize(true)
    destroy()
  }

}
