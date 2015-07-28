package skinny.engine

import javax.servlet.{ ServletContext, ServletContextEvent, ServletContextListener }

import skinny.engine.implicits.RicherStringImplicits
import skinny.logging.LoggerProvider
import SkinnyEngineListener._

class SkinnyEngineListener extends ServletContextListener with LoggerProvider {

  import RicherStringImplicits._

  private[this] var cycle: LifeCycle = _

  private[this] var servletContext: ServletContext = _

  override def contextInitialized(sce: ServletContextEvent): Unit = {
    try {
      configureServletContext(sce)
      configureCycleClass(Thread.currentThread.getContextClassLoader)
    } catch {
      case e: Throwable =>
        logger.error("Failed to initialize skinny application at " + sce.getServletContext.getContextPath, e)
        throw e
    }
  }

  def contextDestroyed(sce: ServletContextEvent): Unit = {
    if (cycle != null) {
      logger.info("Destroying life cycle class: %s".format(cycle.getClass.getName))
      cycle.destroy(servletContext)
    }
  }

  protected def configureExecutionContext(sce: ServletContextEvent): Unit = {
  }

  protected def probeForCycleClass(classLoader: ClassLoader): (String, LifeCycle) = {
    val cycleClassName = {
      Option(servletContext.getInitParameter(LifeCycleKey))
        .flatMap(_.blankOption)
        .getOrElse(DefaultLifeCycle)
    }
    logger.info("The cycle class name from the config: " + (if (cycleClassName == null) "null" else cycleClassName))

    val lifeCycleClass: Class[_] = {
      try { Class.forName(cycleClassName, true, classLoader) }
      catch { case _: ClassNotFoundException => null; case t: Throwable => throw t }
    }
    lazy val oldLifeCycleClass: Class[_] = {
      try { Class.forName(OldDefaultLifeCycle, true, classLoader) }
      catch {
        case _: ClassNotFoundException => null
        case t: Throwable => throw t
      }
    }
    val cycleClass: Class[_] = {
      if (lifeCycleClass != null) lifeCycleClass
      else oldLifeCycleClass
    }

    assert(cycleClass != null, "No skinny.engine.LifeCycle class found!")
    assert(classOf[LifeCycle].isAssignableFrom(cycleClass),
      """No skinny.engine.LifeCycle class found!
        |
        |echo 'import skinny._
        |import _root_.controller._
        |
        |class Bootstrap extends SkinnyLifeCycle {
        |  override def initSkinnyApp(ctx: ServletContext) {
        |    Controllers.mount(ctx)
        |  }
        |}
        |' > src/main/scala/Bootstrap.scala
        |
        |If you're using only skinny-engine, inherit skinny.engine.LifeCycle instead.
        |
        |"""".stripMargin
    )
    logger.debug(s"Loaded lifecycle class: ${cycleClass}")

    if (cycleClass.getName == OldDefaultLifeCycle) {
      logger.warn(s"${OldDefaultLifeCycle} for a boot class will be removed eventually. Please use ${DefaultLifeCycle} instead as class name.")
    }
    (cycleClass.getSimpleName, cycleClass.newInstance.asInstanceOf[LifeCycle])
  }

  protected def configureServletContext(sce: ServletContextEvent): Unit = {
    servletContext = sce.getServletContext
  }

  protected def configureCycleClass(classLoader: ClassLoader): Unit = {
    val (cycleClassName, cycleClass) = probeForCycleClass(classLoader)
    cycle = cycleClass
    logger.info("Initializing life cycle class: %s".format(cycleClassName))
    cycle.init(servletContext)
  }
}

object SkinnyEngineListener {

  val DefaultLifeCycle: String = "Bootstrap"

  // DO NOT RENAME THIS CLASS NAME AS IT BREAKS THE ENTIRE WORLD
  // TOGETHER WITH THE WORLD IT WILL BREAK ALL EXISTING SCALATRA APPS
  // RENAMING THIS CLASS WILL RESULT IN GETTING SHOT, IF YOU SURVIVE YOU WILL BE SHOT AGAIN
  val OldDefaultLifeCycle: String = "ScalatraBootstrap"
  val LifeCycleKey: String = "skinny.engine.LifeCycle"

}
