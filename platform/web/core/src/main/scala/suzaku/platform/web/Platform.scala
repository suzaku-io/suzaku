package suzaku.platform.web

import suzaku.platform.{Logger, Platform, Scheduler}
import suzaku.ui.WidgetRenderer

object WebPlatform extends Platform {
  def logger: Logger = new DOMLogger

  def scheduler: Scheduler = new DOMScheduler

  def widgetRenderer(logger: Logger): WidgetRenderer = new DOMWidgetRenderer(logger)
}
