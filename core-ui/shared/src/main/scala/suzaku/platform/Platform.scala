package suzaku.platform

import suzaku.ui.UIManager

trait Platform {
  def logger: Logger

  def scheduler: Scheduler

  def uiManager(logger: Logger): UIManager
}
