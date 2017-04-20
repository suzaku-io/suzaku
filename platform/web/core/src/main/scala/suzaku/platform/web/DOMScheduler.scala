package suzaku.platform.web

import suzaku.platform.{Cancellable, Scheduler}
import org.scalajs.dom

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js.timers._

class DOMScheduler extends Scheduler {

  private class TimeoutCancellable(handle: SetTimeoutHandle) extends Cancellable {
    var isCancelled = false

    override def cancel(): Unit = {
      isCancelled = true
      clearTimeout(handle)
    }
  }

  private class IntervalCancellable(handle: SetIntervalHandle) extends Cancellable {
    var isCancelled = false

    override def cancel(): Unit = {
      isCancelled = true
      clearInterval(handle)
    }
  }

  private class FrameCancellable extends Cancellable {
    var isCancelled = false

    override def cancel(): Unit = {
      isCancelled = true
    }
  }

  override def scheduleOnce(after: FiniteDuration, callback: ScheduleCB): Cancellable = {
    new TimeoutCancellable(setTimeout(after) {
      val time = (dom.window.performance.now() * 1e6).toLong
      callback(time)
    })
  }

  override def schedule(interval: FiniteDuration, callback: ScheduleCB): Cancellable = {
    new IntervalCancellable(setInterval(interval) {
      val time = (dom.window.performance.now() * 1e6).toLong
      callback(time)
    })
  }

  private def frameCB(cancellable: FrameCancellable, callback: ScheduleCB)(time: Double): Unit = {
    if (!cancellable.isCancelled) {
      callback((time * 1e6).toLong)
      dom.window.requestAnimationFrame(frameCB(cancellable, callback) _)
    }
  }

  override def scheduleFrame(callback: ScheduleCB): Cancellable = {
    val cancellable = new FrameCancellable
    dom.window.requestAnimationFrame(frameCB(cancellable, callback) _)
    cancellable
  }
}
