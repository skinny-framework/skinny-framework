package skinny.engine

import javax.servlet.ServletContext

import skinny.engine.implicits.ServletApiImplicits

trait LifeCycle extends ServletApiImplicits {

  def init(context: ServletContext): Unit = {}

  def destroy(context: ServletContext): Unit = {}

}
