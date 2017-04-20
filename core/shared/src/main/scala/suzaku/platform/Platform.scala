package suzaku.platform

import suzaku.ui.WidgetRenderer

trait Platform {
  def logger: Logger

  def scheduler: Scheduler

  def widgetRenderer(logger: Logger): WidgetRenderer
}
