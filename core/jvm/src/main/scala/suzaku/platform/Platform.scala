package suzaku.platform

import suzaku.platform.server.ServerLogger
import suzaku.ui.WidgetManager

object PlatformImpl extends Platform {
  def logger: Logger = new ServerLogger

  def scheduler: Scheduler = ???

  def widgetRenderer(logger: Logger): WidgetManager = ???
}
