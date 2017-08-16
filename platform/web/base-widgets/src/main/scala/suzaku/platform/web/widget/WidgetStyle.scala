package suzaku.platform.web.widget

import suzaku.platform.web.DOMUIManager
import suzaku.ui.style.ColorProvider

abstract class WidgetStyle(implicit uiManager: DOMUIManager) {
  implicit val colorProvider: ColorProvider = uiManager

  def styleConfig = WidgetStyleConfig.style

  val base: String
}

trait WidgetStyleProvider {
  type Style <: WidgetStyle

  private var _style = Option.empty[Style]

  protected def buildStyle(implicit uiManager: DOMUIManager): Style

  def style(implicit uiManager: DOMUIManager): Style = _style.getOrElse {
    val s = buildStyle
    _style = Some(s)
    s
  }
}
