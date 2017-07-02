package suzaku.platform

trait Logger {
  def debug(message: => String): Unit
  def info(message: => String): Unit
  def warn(message: => String): Unit
  def error(message: => String): Unit
}

object Logger {
  final val LogLevelDebug = 0
  final val LogLevelInfo  = 1
  final val LogLevelWarn  = 2
  final val LogLevelError = 3
  final val LogLevelNone  = 4
}
