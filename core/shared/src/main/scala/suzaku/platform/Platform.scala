package suzaku.platform

import suzaku.ui.WidgetManager

trait Platform {
  def logger: Logger

  def scheduler: Scheduler

  def widgetRenderer(logger: Logger): WidgetManager
}
