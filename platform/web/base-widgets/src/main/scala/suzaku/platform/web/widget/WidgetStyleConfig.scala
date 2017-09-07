package suzaku.platform.web.widget

import suzaku.platform.web.StyleConfig
import suzaku.ui.style._
import suzaku.ui.style.Implicits._

class WidgetStyleConfig extends StyleConfig {
  def inputFontFamily: String            = fontFamily
  def inputFontSize: LengthDimension     = fontSize
  def inputFontWeight: WeightDimension   = fontWeight
  def inputHeight: LengthDimension       = 2.rem
  def inputBorderWidth: LengthDimension  = 1.px
  def inputBorderRadius: LengthDimension = 0.25.rem
  def inputBorderColor: Color            = PaletteRef(Palette.Primary)

  def buttonFontFamily: String            = inputFontFamily
  def buttonFontSize: LengthDimension     = inputFontSize
  def buttonFontWeight: WeightDimension   = inputFontWeight
  def buttonHeight: LengthDimension       = inputHeight
  def buttonBorderRadius: LengthDimension = inputBorderRadius
  def buttonBoxShadow: String             = "inset 0 2px 0 rgba(255, 255, 255, .1), inset 0 -2px 0 rgba(0, 0, 0, .1)"
  def buttonBoxShadowHover: String        = "inset 0 2px 5rem rgba(0, 0, 0, .1), inset 0 -2px 0 rgba(0, 0, 0, .1)"
  def buttonBoxShadowActive: String       = "inset 0 2px 5rem rgba(0, 0, 0, .1), inset 0 2px 0 rgba(0, 0, 0, .1)"
}

object WidgetStyleConfig {
  private var currentStyle = new WidgetStyleConfig

  def style = currentStyle

  def overrideStyles(newStyles: WidgetStyleConfig): Unit = {
    currentStyle = newStyles
  }
}
