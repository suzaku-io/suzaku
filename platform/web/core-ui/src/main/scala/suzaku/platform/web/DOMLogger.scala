package suzaku.platform.web

import org.scalajs.dom
import suzaku.platform.Logger

class DOMLogger extends Logger {
  override def debug(message: => String): Unit = dom.console.log(message)
  override def info(message: => String): Unit  = dom.console.info(message)
  override def warn(message: => String): Unit  = dom.console.warn(message)
  override def error(message: => String): Unit = dom.console.error(message)
}
