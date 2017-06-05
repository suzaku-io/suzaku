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

// style classes
sealed trait StyleClassProperty extends StyleProperty

case class StyleClasses(styles: List[StyleClass]) extends StyleClassProperty

case class InheritClasses(styles: List[StyleClass]) extends StyleClassProperty

case class ExtendClasses(styles: List[StyleClass]) extends StyleClassProperty

case class RemapClasses(styleMap: Map[StyleClass, List[StyleClass]]) extends StyleBaseProperty

object StyleProperty {
  import boopickle.DefaultBasic._
  implicit val styleClassPickler = StyleClassPickler

  val pickler = PicklerGenerator.generatePickler[StyleProperty]
}
