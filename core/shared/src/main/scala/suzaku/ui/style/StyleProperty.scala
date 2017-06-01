package suzaku.ui.style

sealed trait StyleProperty

case object EmptyStyle extends StyleProperty

case class Color(rgb: Int) extends StyleProperty

case class BackgroundColor(rgb: Int) extends StyleProperty

case class ColorAlpha(rgb: Int, alpha: Float) extends StyleProperty

case class BackgroundColorAlpha(rgb: Int, alpha: Float) extends StyleProperty

case class Order(order: Int) extends StyleProperty

