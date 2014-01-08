package skinny.notification

import org.slf4j.LoggerFactory
import javax.servlet.http._

/**
 * Stack trace logging notifier for uncaught exceptions.
 */
class ExceptionLoggingNotifier extends UncaughtExceptionNotifier {

  private[this] val log = LoggerFactory.getLogger(classOf[ExceptionLoggingNotifier])

  override def notify(t: Throwable, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    log.error(t.getMessage, t)
  }

}
