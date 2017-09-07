package suzaku.platform

import suzaku.platform.server.ServerLogger
import suzaku.ui.UIManager

object PlatformImpl extends Platform {
  def logger: Logger = new ServerLogger

  def scheduler: Scheduler = ???

  def widgetManager(logger: Logger): UIManager = ???
}
