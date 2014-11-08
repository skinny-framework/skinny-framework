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

    it("should read application.conf from default") {
      System.setProperty(SkinnyEnv.PropertyKey, "test")
      stringConfigValue("only") should equal(Some("default"))
    }

    it("should read application.conf with type mismatch") {
      System.setProperty(SkinnyEnv.PropertyKey, "test")
      stringConfigValue("iseq") should equal(None)
    }

    it("should read other.conf via sys.props - config.file") {
      try {
        stringConfigValue("other") should equal(None)
        System.setProperty("config.file", "common/src/test/resources/other.conf")
        stringConfigValue("other") should equal(Some("found"))
      } finally {
        System.clearProperty("config.file")
      }
    }

    it("should read other.conf via sys.props - config.resource") {
      try {
        stringConfigValue("other") should equal(None)
        System.setProperty("config.resource", "other.conf")
        stringConfigValue("other") should equal(Some("found"))
      } finally {
        System.clearProperty("config.resource")
      }
    }
  }

}
