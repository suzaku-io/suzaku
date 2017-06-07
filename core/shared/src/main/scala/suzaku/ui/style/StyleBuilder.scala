package suzaku.ui.style

class StyleBuilder[S <: StyleProperty, V](build: V => S) {
  def :=(value: V): S = build(value)
}
class MultiStyleBuilder[S <: StyleProperty, V](build: Seq[V] => S) {
  def :=(values: V*): S = build(values)
}

abstract class TRBLBuilder[A](top: A => StyleBaseProperty,
                              right: A => StyleBaseProperty,
                              bottom: A => StyleBaseProperty,
                              left: A => StyleBaseProperty) {

  def :=(all: A) = StyleSeq(top(all), right(all), bottom(all), left(all))

  def :=(tb: A, lr: A) = StyleSeq(top(tb), right(lr), bottom(tb), left(lr))

  def :=(t: A, lr: A, b: A) = StyleSeq(top(t), right(lr), bottom(b), left(lr))

  def :=(t: A, r: A, b: A, l: A) = StyleSeq(top(t), right(r), bottom(b), left(l))
}

trait StyleBuilders {
  def styleFor[S <: StyleProperty, V](build: V => S)       = new StyleBuilder(build)
  def stylesFor[S <: StyleProperty, V](build: Seq[V] => S) = new MultiStyleBuilder(build)

  // for style classes
  val inheritClass   = styleFor[InheritClasses, StyleClass](styleClass => InheritClasses(List(styleClass)))
  val inheritClasses = stylesFor[InheritClasses, StyleClass](styleClasses => InheritClasses(styleClasses.toList))
  val extendClass    = styleFor[ExtendClasses, StyleClass](styleClass => ExtendClasses(List(styleClass)))
  val extendClasses  = stylesFor[ExtendClasses, StyleClass](styleClasses => ExtendClasses(styleClasses.toList))
  val remapClass     = styleFor[RemapClasses, (StyleClass, StyleClass)](ct => RemapClasses(Map(ct._1 -> (ct._2 :: Nil))))
  val remapClasses   = stylesFor[RemapClasses, (StyleClass, List[StyleClass])](ct => RemapClasses(ct.toMap))

  // regular style definitions
  val color           = styleFor(Color)
  val backgroundColor = styleFor(BackgroundColor)

  val marginLeft   = styleFor(MarginLeft)
  val marginTop    = styleFor(MarginTop)
  val marginRight  = styleFor(MarginRight)
  val marginBottom = styleFor(MarginBottom)
  class MarginBuilder extends TRBLBuilder[LengthDimension](MarginTop, MarginRight, MarginBottom, MarginLeft)
  val margin = new MarginBuilder

  val paddingLeft   = styleFor(PaddingLeft)
  val paddingTop    = styleFor(PaddingTop)
  val paddingRight  = styleFor(PaddingRight)
  val paddingBottom = styleFor(PaddingBottom)
  class PaddingBuilder extends TRBLBuilder[LengthDimension](PaddingTop, PaddingRight, PaddingBottom, PaddingLeft)
  val padding = new PaddingBuilder

  val offsetLeft   = styleFor(OffsetLeft)
  val offsetTop    = styleFor(OffsetTop)
  val offsetRight  = styleFor(OffsetRight)
  val offsetBottom = styleFor(OffsetBottom)
  class OffsetBuilder extends TRBLBuilder[LengthDimension](OffsetTop, OffsetRight, OffsetBottom, OffsetLeft)
  val offset = new OffsetBuilder

  val borderWidthLeft   = styleFor(BorderLeftWidth)
  val borderWidthTop    = styleFor(BorderTopWidth)
  val borderWidthRight  = styleFor(BorderRightWidth)
  val borderWidthBottom = styleFor(BorderBottomWidth)
  class BorderWidthBuilder
      extends TRBLBuilder[WidthDimension](BorderTopWidth, BorderRightWidth, BorderBottomWidth, BorderLeftWidth)
  val borderWidth = new BorderWidthBuilder

  val borderStyleLeft   = styleFor(BorderLeftStyle)
  val borderStyleTop    = styleFor(BorderTopStyle)
  val borderStyleRight  = styleFor(BorderRightStyle)
  val borderStyleBottom = styleFor(BorderBottomStyle)
  class BorderStyleBuilder
      extends TRBLBuilder[LineStyle](BorderTopStyle, BorderRightStyle, BorderBottomStyle, BorderLeftStyle)
  val borderStyle = new BorderStyleBuilder

  val borderColorLeft   = styleFor(BorderLeftColor)
  val borderColorTop    = styleFor(BorderTopColor)
  val borderColorRight  = styleFor(BorderRightColor)
  val borderColorBottom = styleFor(BorderBottomColor)
  class BorderColorBuilder
      extends TRBLBuilder[RGBColor](BorderTopColor, BorderRightColor, BorderBottomColor, BorderLeftColor)
  val borderColor = new BorderColorBuilder
  class BorderBuilder {
    def :=(w: WidthDimension)                            = borderWidth := w
    def :=(w: WidthDimension, s: LineStyle)              = (borderWidth := w) ++ (borderStyle := s)
    def :=(w: WidthDimension, s: LineStyle, c: RGBColor) = (borderWidth := w) ++ (borderStyle := s) ++ (borderColor := c)
  }
  val border = new BorderBuilder

  val outlineWidth = styleFor(OutlineWidth)
  val outlineStyle = styleFor(OutlineStyle)
  val outlineColor = styleFor(OutlineColor)

  class OutlineBuilder {
    def :=(w: WidthDimension)                            = outlineWidth := w
    def :=(w: WidthDimension, s: LineStyle)              = StyleSeq(outlineWidth := w, outlineStyle := s)
    def :=(w: WidthDimension, s: LineStyle, c: RGBColor) = StyleSeq(outlineWidth := w, outlineStyle := s, outlineColor := c)
  }
  val outline = new OutlineBuilder

  // layout and dimensions
  val width     = styleFor(Width)
  val height    = styleFor(Height)
  val maxWidth  = styleFor(MaxWidth)
  val maxHeight = styleFor(MaxHeight)
  val minWidth  = styleFor(MinWidth)
  val minHeight = styleFor(MinHeight)

  val order  = styleFor(Order)
  val zOrder = styleFor(ZOrder)

}
