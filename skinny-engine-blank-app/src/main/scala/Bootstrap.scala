import javax.servlet._
import skinny.engine._
import app._

class Bootstrap extends LifeCycle {
  override def init(ctx: ServletContext) {
    Hello.mount(ctx)
    AsyncHello.mount(ctx)
  }
}
