package suzaku.ui.style

class StyleBuilder[S <: StyleProperty, V](build: V => S) {
  def :=(value: V): S = build(value)
}

trait StyleBuilders {
  val color = new StyleBuilder(Color)

  val backgroundColor = new StyleBuilder(BackgroundColor)
}
