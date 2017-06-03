package suzaku.ui.style

class StyleBuilder[S <: StyleProperty, V](build: V => S) {
  def :=(value: V): S = build(value)
}

trait StyleBuilders {
  def styleFor[S <: StyleProperty, V](build: V => S) = new StyleBuilder(build)

  // for style identifiers
  val inheritClass   = styleFor[InheritClasses, StyleClass](styleClass => InheritClasses(List(styleClass)))
  val inheritClasses = styleFor[InheritClasses, List[StyleClass]](styleClasses => InheritClasses(styleClasses))

  // regular style definitions
  val color           = styleFor(Color)
  val backgroundColor = styleFor(BackgroundColor)

  // layout and dimensions
  val width  = styleFor(Width)
  val height = styleFor(Height)
  val order  = styleFor(Order)
}
