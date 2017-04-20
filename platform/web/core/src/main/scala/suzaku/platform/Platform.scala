package suzaku.platform

import suzaku.platform.web.{DOMLogger, DOMScheduler, DOMWidgetRenderer}
import suzaku.ui.WidgetRenderer

object PlatformImpl extends Platform {
  def logger: Logger = new DOMLogger

  def scheduler: Scheduler = new DOMScheduler

  def widgetRenderer(logger: Logger): WidgetRenderer = new DOMWidgetRenderer(logger)
}
