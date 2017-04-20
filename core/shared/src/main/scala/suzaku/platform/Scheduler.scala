package suzaku.platform

import scala.concurrent.duration.FiniteDuration

trait Scheduler {
  type ScheduleCB = Long => Unit

  def scheduleOnce(after: FiniteDuration, callback: ScheduleCB): Cancellable

  def schedule(interval: FiniteDuration, callback: ScheduleCB): Cancellable

  def scheduleFrame(callback: ScheduleCB): Cancellable
}

trait Cancellable {
  def cancel(): Unit

  def isCancelled: Boolean
}