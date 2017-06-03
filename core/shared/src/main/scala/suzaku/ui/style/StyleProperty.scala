package suzaku.ui.style

import boopickle.Default._

sealed trait StyleProperty

sealed trait StyleBaseProperty extends StyleProperty

case object EmptyStyle extends StyleBaseProperty

case class Color(color: RGBColor) extends StyleBaseProperty

case class BackgroundColor(color: RGBColor) extends StyleBaseProperty

// Layout related styles
case class Order(order: Int) extends StyleBaseProperty

case class Width(value: LengthUnit) extends StyleBaseProperty

case class Height(value: LengthUnit) extends StyleBaseProperty

// style identifiers
case class StyleClasses(styles: List[StyleClass]) extends StyleProperty

case class InheritClasses(styles: List[StyleClass]) extends StyleProperty

object StyleProperty {
  import boopickle.DefaultBasic._
  implicit val styleIdPickler = StyleIdPickler

  val pickler = PicklerGenerator.generatePickler[StyleProperty]
}
