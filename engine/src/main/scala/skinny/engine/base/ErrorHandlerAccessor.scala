package skinny.engine.base

import java.util.concurrent.atomic.AtomicInteger

import skinny.engine._
import skinny.logging.LoggerProvider

trait ErrorHandlerAccessor extends LoggerProvider {

  /**
   * The error handler function, called if an exception is thrown during
   * before filters or the routes.
   */
  private[this] var errorHandler: ErrorHandler = {
    case t => throw t
  }

  protected def currentErrorHandler: ErrorHandler = errorHandler

  /**
   * Count execution of error filter registration.
   */
  private[this] lazy val errorMethodCallCountAtSkinnyEngineBase: AtomicInteger = new AtomicInteger(0)

  /**
   * Detects error filter leak issue as an error.
   */
  protected def detectTooManyErrorFilterRegistrationAsAnErrorAtSkinnyEngineBase: Boolean = false

  // https://github.com/scalatra/scalatra/blob/v2.3.1/core/src/main/scala/org/scalatra/ScalatraBase.scala#L333-L335
  def error(handler: ErrorHandler): Unit = {
    val count = errorMethodCallCountAtSkinnyEngineBase.incrementAndGet()
    if (count > 500) {
      val message = s"skinny's error filter registration for this controller has been evaluated $count times, this behavior will cause memory leak."
      if (detectTooManyErrorFilterRegistrationAsAnErrorAtSkinnyEngineBase) throw new RuntimeException(message)
      else logger.warn(message)
    }
    errorHandler = handler orElse errorHandler
  }

}
