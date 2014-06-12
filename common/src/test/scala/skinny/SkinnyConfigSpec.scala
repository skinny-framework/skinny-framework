package skinny

import org.scalatest._

class SkinnyConfigSpec extends FunSpec with Matchers with SkinnyConfig {

  describe("SkinnyConfing#values") {

    it("should read application.conf") {
      System.setProperty(SkinnyEnv.PropertyKey, "test")
      stringConfigValue("foo") should equal(Some("bar"))
      booleanConfigValue("ok") should equal(Some(false))
      intConfigValue("numbers.num") should equal(Some(123))
      longConfigValue("numbers.num") should equal(Some(123))
      doubleConfigValue("numbers.double") should equal(Some(1.2d))

      intSeqConfigValue("iseq") should equal(Some(Seq(1, 2, 3)))
      longSeqConfigValue("iseq") should equal(Some(Seq(1, 2, 3)))
      stringSeqConfigValue("strseq") should equal(Some(Seq("A", "b", "c")))
    }
  }

}
