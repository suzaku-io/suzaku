package suzaku

import suzaku.platform.Logger
import org.scalatest._

abstract class UnitSpec extends WordSpec with Matchers {}

object TestLogger extends Logger {
  override def debug(message: => String): Unit = println(message)
  override def info(message: => String): Unit  = println(message)
  override def warn(message: => String): Unit  = println(message)
  override def error(message: => String): Unit = println(message)
}
