package suzaku.ui.style

sealed trait StyleProperty

case object EmptyStyle extends StyleProperty

case class Color(color: RGBColor) extends StyleProperty

case class BackgroundColor(color: RGBColor) extends StyleProperty

// Layout related styles
case class Order(order: Int) extends StyleProperty

case class Width(value: LengthUnit) extends StyleProperty

case class Height(value: LengthUnit) extends StyleProperty

