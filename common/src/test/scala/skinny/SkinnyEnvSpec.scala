package skinny

import org.scalatest._

class SkinnyEnvSpec extends FlatSpec with Matchers {

  behavior of "SkinnyEnv"

  // affects other tests

  //  it should "work with prop value" in {
  //    System.setProperty(SkinnyEnv.PropertyKey, "foo1")
  //    System.clearProperty(SkinnyEnv.EnvKey)
  //    SkinnyEnv.get() should equal(Some("foo1"))
  //
  //    System.clearProperty(SkinnyEnv.PropertyKey)
  //    System.setProperty(SkinnyEnv.EnvKey, "foo2")
  //    SkinnyEnv.get() should equal(Some("foo2"))
  //  }
  //
  //  it should "work with env value" in {
  //    System.clearProperty(SkinnyEnv.PropertyKey)
  //    System.clearProperty(SkinnyEnv.EnvKey)
  //    val env = SkinnyEnv.get()
  //    println("SKINNY_ENV: " + env)
  //  }

}
