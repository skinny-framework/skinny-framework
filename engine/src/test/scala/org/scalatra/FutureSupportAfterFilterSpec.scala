package org.scalatra

import _root_.akka.actor.ActorSystem
import org.scalatra.test.specs2.MutableScalatraSpec
import skinny.engine.SkinnyEngineServlet

import scala.concurrent.Future

class FutureSupportAfterFilterServlet extends SkinnyEngineServlet {
  val system = ActorSystem()
  var actionTime: Long = _
  var afterTime: Long = _
  var afterCount: Long = _
  protected override implicit val executionContext = system.dispatcher

  asyncGet("/async") {
    Thread.sleep(2000)
    actionTime = System.nanoTime()

    "async"
  }

  asyncGet("/async-two-afters") {
    Thread.sleep(2000)
    "async-two-afters"
  }

  get("/sync") {
    "sync"
  }

  get("/future") {
    Future({
      "future"
    })
  }

  after() {
    afterCount += 1
    afterTime = System.nanoTime()
  }

  after("/async-two-afters") {
    afterCount += 1
    afterTime = System.nanoTime()
  }

  def reset() {
    actionTime = 0L
    afterTime = 0L
    afterCount = 0L
  }

  override def destroy() {
    super.destroy()
    system.shutdown()
  }
}

class FutureSupportAfterFilterSpec extends MutableScalatraSpec {
  sequential

  val servlet = new FutureSupportAfterFilterServlet()
  addServlet(servlet, "/foo/*")
  addServlet(servlet, "/*")

  "after filters for asynchronous actions" should {
    "run after the action" in {
      servlet.reset()

      get("/async") {
        servlet.afterTime must beGreaterThan(servlet.actionTime)
      }
    }

    "run only once" in {
      servlet.reset()

      get("/async") {
        servlet.afterCount mustEqual 1
      }
    }

    "all execute" in {
      servlet.reset()

      get("/async-two-afters") {
        servlet.afterCount mustEqual 2
      }
    }

    "work when contextPath != /" in {
      servlet.reset()

      get("/foo/async-two-afters") {
        servlet.afterCount mustEqual 2
      }
    }
  }

  "after filters for synchronous get with Future return value" should {
    "run after the action" in {
      servlet.reset()

      get("/future") {
        servlet.afterTime must beGreaterThan(servlet.actionTime)
      }
    }

    "run only once" in {
      servlet.reset()

      get("/future") {
        servlet.afterCount mustEqual 1
      }
    }
  }

  "after filters for synchronous actions" should {
    "run normally" in {
      servlet.reset()

      get("/sync") {
        servlet.afterCount mustEqual 1
      }
    }
  }
}
