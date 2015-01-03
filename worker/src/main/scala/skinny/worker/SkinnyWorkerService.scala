package skinny.worker

import java.util.concurrent._
import org.joda.time.DateTime
import skinny.logging.Logging

/**
 * Service which manages workers.
 */
case class SkinnyWorkerService(name: String = "skinny-framework-worker-service", threadPoolSize: Int = 10) extends Logging {

  logger.info(s"SkinnyWorkerService (name: ${name}) is activated.")

  /**
   * Thread pool for this worker service.
   */
  private[this] val pool = Executors.newScheduledThreadPool(threadPoolSize, new ThreadFactory() {
    val threadGroup = new ThreadGroup(name)
    def newThread(r: Runnable): Thread = {
      val t = new Thread(threadGroup, r);
      t.setDaemon(true)
      t.setName(t.getThreadGroup.getName + "-thread-" + t.getId())
      t
    }
  })

  /**
   * Registers new worker to this service.
   */
  def registerSkinnyWorker(worker: SkinnyWorker, initial: Int, interval: Int, timeUnit: TimeUnit = TimeUnit.SECONDS) = {
    pool.scheduleAtFixedRate(worker, initial, interval, timeUnit)

    logger.debug(s"New worker has been scheduled. " +
      s"(class: ${worker.getClass.getCanonicalName}, initial: ${initial}, interval: ${interval}, time unit: ${timeUnit})")
  }

  /**
   * Schedules this worker every fixed milliseconds.
   */
  def everyFixedMilliseconds(worker: SkinnyWorker, interval: Int) = {
    registerSkinnyWorker(worker, 100, interval, TimeUnit.MILLISECONDS)
  }

  /**
   * Schedules this worker every fixed seconds.
   */
  def everyFixedSeconds(worker: SkinnyWorker, interval: Int) = {
    registerSkinnyWorker(worker, 1, interval, TimeUnit.SECONDS)
  }

  /**
   * Schedules this worker every fixed seconds.
   */
  def everyFixedMinutes(worker: SkinnyWorker, interval: Int) = {
    registerSkinnyWorker(worker, 1, (interval * 60), TimeUnit.SECONDS)
  }

  /**
   * Schedules this worker hourly.
   */
  def hourly(worker: SkinnyWorker, fixedMinute: Int = 0) = {
    val scheduledDate = {
      val date = DateTime.now.withMinuteOfHour(fixedMinute)
      if (date.isAfterNow) date else date.plusHours(1)
    }
    val initialSeconds = ((scheduledDate.getMillis - DateTime.now.getMillis) / 1000).toInt
    registerSkinnyWorker(worker, initialSeconds, 3600, TimeUnit.SECONDS)
  }

  /**
   * Schedules this worker daily.
   */
  def daily(worker: SkinnyWorker, fixedHour: Int = 9, fixedMinute: Int = 0) = {
    val scheduledDate = {
      val date = DateTime.now.withHourOfDay(fixedHour).withMinuteOfHour(fixedMinute)
      if (date.isAfterNow) date else date.plusDays(1)
    }
    val initialSeconds = ((scheduledDate.getMillis - DateTime.now.getMillis) / 1000).toInt
    registerSkinnyWorker(worker, initialSeconds, 3600 * 24, TimeUnit.SECONDS)
  }

  /**
   * Shutdown this worker service safely.
   */
  def shutdownNow(awaitSeconds: Int = 10) = {
    // disable new tasks from being submitted
    pool.shutdown()
    try {
      // cancel currently executing tasks
      pool.shutdownNow()
      // wait a while for tasks to respond to being cancelled
      if (!pool.awaitTermination(awaitSeconds, TimeUnit.SECONDS)) {
        logger.warn("Failed to terminate all worker thread")
      } else {
        logger.info(s"SkinnyWorkerService (name: ${name}) is abandoned safely.")
      }
    } catch {
      case e: InterruptedException =>
        // (re-) cancel if current thread also interrupted
        pool.shutdownNow()
        // preserve interrupt status
        logger.info(s"SkinnyWorkerService (name: ${name}) will be interrupted.")
        Thread.currentThread.interrupt()
    }
  }

}
