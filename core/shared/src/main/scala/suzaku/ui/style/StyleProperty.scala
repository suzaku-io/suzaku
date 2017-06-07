package suzaku.ui.style

import boopickle.Default._

sealed trait StyleDef

sealed trait StyleProperty extends StyleDef

sealed trait StyleBaseProperty extends StyleProperty

// meta styles
case class StyleSeq(styles: List[StyleBaseProperty]) extends StyleDef {
  def ++(other: StyleSeq): StyleSeq = StyleSeq(styles ::: other.styles)
}

object StyleSeq {
  def apply(style: StyleBaseProperty*): StyleSeq = StyleSeq(style.toList)
}

// normal style properties
case object EmptyStyle extends StyleBaseProperty

case class Color(color: RGBColor) extends StyleBaseProperty

case class BackgroundColor(color: RGBColor) extends StyleBaseProperty

sealed trait Direction
trait DirectionTop    extends Direction
trait DirectionRight  extends Direction
trait DirectionBottom extends Direction
trait DirectionLeft   extends Direction

sealed trait Margin extends Direction with StyleBaseProperty {
  def value: LengthDimension
}

case class MarginLeft(value: LengthDimension)   extends StyleBaseProperty with Margin with DirectionLeft
case class MarginTop(value: LengthDimension)    extends StyleBaseProperty with Margin with DirectionTop
case class MarginRight(value: LengthDimension)  extends StyleBaseProperty with Margin with DirectionRight
case class MarginBottom(value: LengthDimension) extends StyleBaseProperty with Margin with DirectionBottom

sealed trait Padding extends Direction with StyleBaseProperty {
  def value: LengthDimension
}

case class PaddingLeft(value: LengthDimension)   extends StyleBaseProperty with Padding with DirectionLeft
case class PaddingTop(value: LengthDimension)    extends StyleBaseProperty with Padding with DirectionTop
case class PaddingRight(value: LengthDimension)  extends StyleBaseProperty with Padding with DirectionRight
case class PaddingBottom(value: LengthDimension) extends StyleBaseProperty with Padding with DirectionBottom

sealed trait Offset extends Direction with StyleBaseProperty {
  def value: LengthDimension
}

case class OffsetLeft(value: LengthDimension)   extends StyleBaseProperty with Offset with DirectionLeft
case class OffsetTop(value: LengthDimension)    extends StyleBaseProperty with Offset with DirectionTop
case class OffsetRight(value: LengthDimension)  extends StyleBaseProperty with Offset with DirectionRight
case class OffsetBottom(value: LengthDimension) extends StyleBaseProperty with Offset with DirectionBottom

sealed trait BorderWidth extends Direction with StyleBaseProperty {
  def value: WidthDimension
}

case class BorderLeftWidth(value: WidthDimension)   extends StyleBaseProperty with BorderWidth with DirectionLeft
case class BorderTopWidth(value: WidthDimension)    extends StyleBaseProperty with BorderWidth with DirectionTop
case class BorderRightWidth(value: WidthDimension)  extends StyleBaseProperty with BorderWidth with DirectionRight
case class BorderBottomWidth(value: WidthDimension) extends StyleBaseProperty with BorderWidth with DirectionBottom

sealed trait BorderStyle extends Direction with StyleBaseProperty {
  def style: LineStyle
}

case class BorderLeftStyle(style: LineStyle)   extends StyleBaseProperty with BorderStyle with DirectionLeft
case class BorderTopStyle(style: LineStyle)    extends StyleBaseProperty with BorderStyle with DirectionTop
case class BorderRightStyle(style: LineStyle)  extends StyleBaseProperty with BorderStyle with DirectionRight
case class BorderBottomStyle(style: LineStyle) extends StyleBaseProperty with BorderStyle with DirectionBottom

sealed trait BorderColor extends Direction with StyleBaseProperty {
  def color: RGBColor
}

case class BorderLeftColor(color: RGBColor)   extends StyleBaseProperty with BorderColor with DirectionLeft
case class BorderTopColor(color: RGBColor)    extends StyleBaseProperty with BorderColor with DirectionTop
case class BorderRightColor(color: RGBColor)  extends StyleBaseProperty with BorderColor with DirectionRight
case class BorderBottomColor(color: RGBColor) extends StyleBaseProperty with BorderColor with DirectionBottom

case class OutlineWidth(value: WidthDimension) extends StyleBaseProperty
case class OutlineStyle(style: LineStyle)      extends StyleBaseProperty
case class OutlineColor(color: RGBColor)       extends StyleBaseProperty

// Layout related styles
case class Order(order: Int)  extends StyleBaseProperty
case class ZOrder(order: Int) extends StyleBaseProperty

// dimension styles
case class Width(value: LengthDimension)  extends StyleBaseProperty
case class Height(value: LengthDimension) extends StyleBaseProperty

case class MaxWidth(value: LengthDimension)  extends StyleBaseProperty
case class MaxHeight(value: LengthDimension) extends StyleBaseProperty

case class MinWidth(value: LengthDimension)  extends StyleBaseProperty
case class MinHeight(value: LengthDimension) extends StyleBaseProperty

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
