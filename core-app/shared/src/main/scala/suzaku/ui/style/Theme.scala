package suzaku.ui.style

import suzaku.ui.WidgetProtocolProvider

import scala.language.implicitConversions

case class Theme(styleMap: Map[WidgetProtocolProvider, List[StyleClass]])

object Theme {
  def apply(styleMapping: (WidgetProtocolProvider, List[StyleClass])*): Theme = Theme(styleMapping.toMap)
}

trait ThemeImplicits {
  implicit def singleClass2Theme(i: (WidgetProtocolProvider, StyleClass)): (WidgetProtocolProvider, List[StyleClass]) = {
    (i._1, i._2 :: Nil)
  }
}
