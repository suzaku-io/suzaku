package suzaku.ui.style

import scala.language.implicitConversions

sealed trait RGBColor {
  val rgb: Int
  def r: Int = (rgb >> 16) & 0xFF
  def g: Int = (rgb >> 8) & 0xFF
  def b: Int = rgb & 0xFF
}

case class RGB(rgb: Int) extends RGBColor

case class RGBA(rgb: Int, a: Double) extends RGBColor

trait Colors {
  def rgb(r: Int, g: Int, b: Int) = RGB(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF))

  def rgba(r: Int, g: Int, b: Int, a: Double) = RGBA(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF), a)

  implicit def int2color(i: Int): RGB = RGB(i)
}
