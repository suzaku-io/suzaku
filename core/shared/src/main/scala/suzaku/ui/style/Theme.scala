package suzaku.ui.style

import suzaku.ui.WidgetBlueprintProvider

case class Theme(styleMap: Map[WidgetBlueprintProvider, List[StyleClass]])

object Theme {
  def apply(styleMapping: (WidgetBlueprintProvider, List[StyleClass])*): Theme = Theme(styleMapping.toMap)
}
