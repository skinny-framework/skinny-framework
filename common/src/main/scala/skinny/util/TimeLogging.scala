package skinny.util

import skinny.logging.Logging
import skinny.SkinnyEnv

object TimeLogging extends TimeLogging

/**
 * Time logging for the block.
 *
 * {{{
 * class RootController extends ApplicationController {
 * def doSomething = {
 * Thread.sleep(10)
 * "AAAA"
 * }
 *
 * def index = {
 * val result = warnElapsedTime(1) {
 * doSomething
 * }
 * render("/root/index")
 * }
 * }}}
 */
trait TimeLogging extends Logging {

  protected def stackTraceDepthForTimeLogging: Int = 5

  def warnElapsedTime[A](millis: Long, additionalLines: => Seq[String] = Nil)(action: => A) = {
    def where: String = {
      s"""
      |${Thread.currentThread.getStackTrace.drop(7).take(stackTraceDepthForTimeLogging).map { callee => s"  ${callee}" }.mkString("\n")}
      |  ...
      |""".stripMargin
    }

    val before = System.currentTimeMillis
    val result = action
    val after = System.currentTimeMillis
    val elapsedMillis = after - before
    def additionalInfo = if (additionalLines.isEmpty) "" else "\n" + additionalLines.map(s => "  " + s).mkString("\n")
    if (elapsedMillis >= millis) {
      logger.warn(s"[SLOW EXECUTION DETECTED] Elapsed time: ${elapsedMillis} millis${additionalInfo}\n${where}")
    } else {
      if (!SkinnyEnv.isProduction()) {
        logger.info(s"Elapsed time: ${elapsedMillis} millis${additionalInfo}\n${where}")
      }
    }
    result
  }

}
