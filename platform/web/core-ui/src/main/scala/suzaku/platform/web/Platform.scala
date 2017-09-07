package suzaku.platform.web

import suzaku.platform.{Logger, Platform, Scheduler}
import suzaku.ui.UIManager

object WebPlatform extends Platform {
  def logger: Logger = new DOMLogger

  def scheduler: Scheduler = new DOMScheduler

  def widgetManager(logger: Logger): UIManager = new DOMUIManager(logger, this)
}
