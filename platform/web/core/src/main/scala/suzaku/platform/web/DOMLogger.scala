package suzaku.platform.web

import suzaku.platform.Logger
import org.scalajs.dom

class DOMLogger extends Logger {
  override def debug(message: => String): Unit = dom.console.log(message)
  override def info(message: => String): Unit  = dom.console.info(message)
  override def warn(message: => String): Unit  = dom.console.warn(message)
  override def error(message: => String): Unit = dom.console.error(message)
}
