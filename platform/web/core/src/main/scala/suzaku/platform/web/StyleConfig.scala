package suzaku.platform.web

class StyleConfig {
  import suzaku.ui.style._

  def fontSystem: String          = "system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Oxygen, Ubuntu, Cantarell, Fira Sans, Droid Sans, Helvetica Neue"
  def fontFamily: String          = fontSystem
  def fontSize: LengthDimension   = 1.rem
  def fontWeight: WeightDimension = WeightValue(400)

  def lineHeight: Double = 1.5
}

object StyleConfig {
  private var currentStyle = new StyleConfig

  def style = currentStyle

  def overrideStyles(newStyles: StyleConfig): Unit = {
    currentStyle = newStyles
  }
}
