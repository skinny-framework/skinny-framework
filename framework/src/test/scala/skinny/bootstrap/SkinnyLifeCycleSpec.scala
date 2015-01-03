package skinny.bootstrap

import javax.servlet.ServletContext

import org.scalatest._

class SkinnyLifeCycleSpec extends FunSpec with Matchers {

  val lifeCycle = new SkinnyLifeCycle {
    override def initSkinnyApp(ctx: ServletContext): Unit = {
    }
  }

  val lifeCycleWithoutWorkerAndDB = new SkinnyLifeCycle {
    override def workerServiceEnabled = false
    override def dbSettingsEnabled = false
    override def initSkinnyApp(ctx: ServletContext): Unit = {}
  }

  describe("#skinnyWorkerService") {
    it("should be available when enabled") {
      lifeCycle.skinnyWorkerService should not equal (null)
    }
    it("should be unavailable when disabled") {
      intercept[IllegalStateException] {
        lifeCycleWithoutWorkerAndDB.skinnyWorkerService
      }
    }
  }

  describe("#dbSettingsEnabled") {
    it("should be available") {
      lifeCycle.dbSettingsEnabled should equal(true)
      lifeCycle.dbSettingsRequired should equal(true)
    }
  }

  describe("#init") {
    it("should be available") {
      lifeCycle.init(null)
      lifeCycleWithoutWorkerAndDB.init(null)
    }
  }

  describe("#destroy") {
    it("should be available") {
      lifeCycle.destroy(null)
      lifeCycleWithoutWorkerAndDB.destroy(null)
    }
  }

}
