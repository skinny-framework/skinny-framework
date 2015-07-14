package skinny.engine.async

import java.util.concurrent.atomic.AtomicBoolean
import javax.servlet.{ AsyncEvent, AsyncListener }

import skinny.engine._
import skinny.engine.control.HaltException
import skinny.engine.routing.AsyncRoutingDsl

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

trait FutureSupport extends SkinnyEngineBase with AsyncRoutingDsl {

  implicit protected def executor: ExecutionContext

  override def asynchronously(f: => Any): Action = () => Future(f)

  // TODO: fix this
  // Still thinking of the best way to specify this before making it public.
  // In the meantime, this gives us enough control for our test.
  // IPC: it may not be perfect but I need to be able to configure this timeout in an application
  // This is a Duration instead of a timeout because a duration has the concept of infinity
  @deprecated("Override the `timeout` method on a `skinny.engine.AsyncResult` instead.", "1.4")
  protected def asyncTimeout: Duration = 30 seconds

  override protected def isAsyncExecutable(result: Any): Boolean = {
    classOf[Future[_]].isAssignableFrom(result.getClass) ||
      classOf[AsyncResult].isAssignableFrom(result.getClass)
  }

  override protected def renderResponse(actionResult: Any): Unit = {
    actionResult match {
      case r: AsyncResult => handleFuture(r.is, r.timeout)
      case f: Future[_] => handleFuture(f, asyncTimeout)
      case a => super.renderResponse(a)
    }
  }

  private[this] def handleFuture(f: Future[_], timeout: Duration): Unit = {
    val gotResponseAlready = new AtomicBoolean(false)
    val context = mainThreadRequest.startAsync(mainThreadRequest, mainThreadResponse)
    if (timeout.isFinite()) context.setTimeout(timeout.toMillis) else context.setTimeout(-1)

    def renderFutureResult(f: Future[_]): Unit = {
      f onComplete {
        // Loop until we have a non-future result
        case Success(f2: Future[_]) => renderFutureResult(f2)
        case Success(r: AsyncResult) => renderFutureResult(r.is)
        case t => {

          if (gotResponseAlready.compareAndSet(false, true)) {
            withinAsyncContext(context) {
              try {
                t map { result =>
                  renderResponse(result)
                } recover {
                  case e: HaltException =>
                    renderHaltException(e)
                  case e =>
                    try {
                      renderResponse(errorHandler(e))
                    } catch {
                      case e: Throwable =>
                        SkinnyEngineBase.runCallbacks(Failure(e))
                        renderUncaughtException(e)
                        SkinnyEngineBase.runRenderCallbacks(Failure(e))
                    }
                }
              } finally {
                context.complete()
              }
            }
          }
        }
      }
    }

    context addListener new AsyncListener {

      def onTimeout(event: AsyncEvent): Unit = {
        onAsyncEvent(event) {
          if (gotResponseAlready.compareAndSet(false, true)) {
            renderHaltException(HaltException(Some(504), None, Map.empty, "Gateway timeout"))
            event.getAsyncContext.complete()
          }
        }
      }

      def onComplete(event: AsyncEvent): Unit = {}

      def onError(event: AsyncEvent): Unit = {
        onAsyncEvent(event) {
          if (gotResponseAlready.compareAndSet(false, true)) {
            event.getThrowable match {
              case e: HaltException => renderHaltException(e)
              case e =>
                try {
                  renderResponse(errorHandler(e))
                } catch {
                  case e: Throwable =>
                    SkinnyEngineBase.runCallbacks(Failure(e))
                    renderUncaughtException(e)
                    SkinnyEngineBase.runRenderCallbacks(Failure(e))
                }
            }
          }
        }
      }

      def onStartAsync(event: AsyncEvent): Unit = {}
    }

    renderFutureResult(f)
  }

}

