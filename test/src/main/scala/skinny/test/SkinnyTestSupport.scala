package skinny.test

import org.scalatra.test._
import skinny.controller.SessionInjectorController

trait SkinnyTestSupport { self: ScalatraTests =>

  object SessionInjector extends SessionInjectorController {
    put("/tmp/SkinnyTestSupport/session")(update)
  }

  addFilter(SessionInjector, "/*")

  def withSession[A](attributes: (String, String)*)(action: => A): A = session {
    put("/tmp/SkinnyTestSupport/session", attributes)()
    action
  }
}
