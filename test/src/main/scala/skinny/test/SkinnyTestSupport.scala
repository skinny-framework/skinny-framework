package skinny.test

import org.scalatra.test._
import skinny.controller.SessionInjectorController
import skinny.SkinnyEnv

trait SkinnyTestSupport { self: ScalatraTests =>

  System.setProperty(SkinnyEnv.Key, "test")

  object SessionInjector extends SessionInjectorController {
    put("/tmp/SkinnyTestSupport/session")(update)
  }

  addFilter(SessionInjector, "/*")

  def withSession[A](attributes: (String, String)*)(action: => A): A = session {
    put("/tmp/SkinnyTestSupport/session", attributes)()
    action
  }
}
