package skinny.worker

import skinny.logging.Logging

trait SkinnyWorker extends Runnable with Logging {

  def execute(): Unit

  def run(): Unit = {
    try execute()
    catch { case t: Throwable => handle(t) }
  }

  def handle(t: Throwable): Unit = {
    logger.error(s"Failed to run ${this.getClass.getCanonicalName} because ${t.getMessage}", t)
  }

}
