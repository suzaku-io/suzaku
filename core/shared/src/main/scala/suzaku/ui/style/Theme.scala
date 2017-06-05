package suzaku.ui.style

import suzaku.ui.WidgetBlueprintProvider
import scala.language.implicitConversions

case class Theme(styleMap: Map[WidgetBlueprintProvider, List[StyleClass]])

object Theme {
  def apply(styleMapping: (WidgetBlueprintProvider, List[StyleClass])*): Theme = Theme(styleMapping.toMap)
}

trait ThemeImplicits {
  implicit def singleClass2Theme(i: (WidgetBlueprintProvider, StyleClass)): (WidgetBlueprintProvider, List[StyleClass]) = {
    (i._1, i._2 :: Nil)
  }
}
