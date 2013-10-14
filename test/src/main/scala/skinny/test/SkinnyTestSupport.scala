package skinny.test

import org.scalatra.test._
import skinny.controller.SessionInjectorController
import skinny.SkinnyEnv

/**
 * Skinny framework testing support
 */
trait SkinnyTestSupport { self: ScalatraTests =>

  // set skinny.env as "test"
  System.setProperty(SkinnyEnv.Key, "test")

  /**
   * Session injector controller
   */
  object SessionInjector extends SessionInjectorController {
    put("/tmp/SkinnyTestSupport/session")(update)
  }
  addFilter(SessionInjector, "/*")

  /**
   * Provides a code block with injected session.
   *
   * @param attributes attributes to inject
   * @param action code block
   * @tparam A return type
   * @return result
   */
  def withSession[A](attributes: (String, String)*)(action: => A): A = session {
    put("/tmp/SkinnyTestSupport/session", attributes)()
    action
  }

}
