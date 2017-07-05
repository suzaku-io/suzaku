package suzaku.ui.style

import scala.language.implicitConversions

sealed trait Color

trait ColorObject {
  @inline def clamp(value: Double, lower: Double, upper: Double): Double =
    lower max value min upper
}

sealed trait AbsoluteColor extends Color {
  def alpha: Double

  private def interpolate(v1: Double, v2: Double, t: Double): Double =
    v1 + (v2 - v1) * t

  def interpolate(other: AbsoluteColor, t: Double): AbsoluteColor = {
    val c1 = toLAB
    val c2 = other.toLAB
    LAB.validated(interpolate(c1.l, c2.l, t),
                  interpolate(c1.a, c2.a, t),
                  interpolate(c1.b, c2.b, t),
                  interpolate(c1.alpha, c2.alpha, t))
  }

  def interpolateRGB(other: AbsoluteColor, t: Double): AbsoluteColor = {
    val c1 = toRGB
    val c2 = other.toRGB
    RGB.validated(interpolate(c1.r, c2.r, t),
                  interpolate(c1.g, c2.g, t),
                  interpolate(c1.b, c2.b, t),
                  interpolate(c1.alpha, c2.alpha, t))
  }

  def toLAB = {
    var XYZ(x, y, z, alpha) = toXYZ

    x /= 95.047
    y /= 100
    z /= 108.883

    x = if (x > 0.008856) math.pow(x, 1.0 / 3) else (7.787 * x) + (16 / 116)
    y = if (y > 0.008856) math.pow(y, 1.0 / 3) else (7.787 * y) + (16 / 116)
    z = if (z > 0.008856) math.pow(z, 1.0 / 3) else (7.787 * z) + (16 / 116)

    val l = (116 * y) - 16
    val a = 500 * (x - y)
    val b = 200 * (y - z)
    LAB(l, a, b, alpha)
  }

  def toXYZ: XYZ

  def toRGBA: RGBAlpha

  def toRGB: RGB = RGB(toRGBA.rgb)

  def toHSL: HSL = toRGBA.toHSL
}

sealed trait RGBColor extends AbsoluteColor {
  val rgb: Int
  def r: Int = (rgb >> 16) & 0xFF
  def g: Int = (rgb >> 8) & 0xFF
  def b: Int = rgb & 0xFF
  def alpha: Double

  override def toXYZ = {
    var r = this.r / 255.0
    var g = this.g / 255.0
    var b = this.b / 255.0

    r = if (r > 0.04045) math.pow((r + 0.055) / 1.055, 2.4) else r / 12.92
    g = if (g > 0.04045) math.pow((g + 0.055) / 1.055, 2.4) else g / 12.92
    b = if (b > 0.04045) math.pow((b + 0.055) / 1.055, 2.4) else b / 12.92

    val x = (r * 0.4124) + (g * 0.3576) + (b * 0.1805)
    val y = (r * 0.2126) + (g * 0.7152) + (b * 0.0722)
    val z = (r * 0.0193) + (g * 0.1192) + (b * 0.9505)

    XYZ(x * 100, y * 100, z * 100, alpha)
  }

  override def toHSL = {
    val rr = this.r / 255.0
    val gg = this.g / 255.0
    val bb = this.b / 255.0

    val max = rr max gg max bb
    val min = rr min gg min bb

    val l = (max + min) / 2.0

    val (h, s) =
      if (max == min) (0.0, 0.0)
      else {
        val d = max - min
        val s = if (l > 0.5) d / (2.0 - max - min) else d / (max + min)
        val h =
          if (rr > gg && rr > bb) (gg - bb) / d + (if (gg < bb) 6.0 else 0.0)
          else if (gg > bb) (bb - rr) / d + 2.0
          else (rr - gg) / d + 4.0
        (h / 6, s)
      }
    HSL(h, s, l, alpha)
  }
}

case class RGB(rgb: Int) extends RGBColor {
  override def alpha  = 1.0
  override def toRGBA = RGBAlpha(rgb, alpha)

  override def toString = s"RGB($r, $g, $b)"
}

case class RGBAlpha(rgb: Int, alpha: Double = 1.0) extends RGBColor {
  override def toRGBA = this

  override def toString = s"RGBAlpha($r, $g, $b, $alpha)"
}

object RGB extends ColorObject {
  def validated(r: Double, g: Double, b: Double, alpha: Double = 1.0): RGBAlpha =
    Colors.rgba(clamp(r, 0, 1), clamp(g, 0, 1), clamp(b, 0, 1), clamp(alpha, 0, 1))
}

case class LAB(l: Double, a: Double, b: Double, alpha: Double = 1.0) extends AbsoluteColor {
  def brightness(amount: Int) = LAB.validated(l + amount, a, b, alpha)

  override def toLAB = this

  override def toXYZ = {
    var y = (l + 16) / 116
    var x = a / 500 + y
    var z = y - b / 200

    val y2 = math.pow(y, 3)
    val x2 = math.pow(x, 3)
    val z2 = math.pow(z, 3)
    y = if (y2 > 0.008856) y2 else (y - 16 / 116) / 7.787
    x = if (x2 > 0.008856) x2 else (x - 16 / 116) / 7.787
    z = if (z2 > 0.008856) z2 else (z - 16 / 116) / 7.787

    x *= 95.047
    y *= 100
    z *= 108.883
    XYZ(x, y, z, alpha)
  }

  override def toRGBA = toXYZ.toRGBA
}

object LAB extends ColorObject {
  def validated(l: Double, a: Double, b: Double, alpha: Double = 1.0): LAB =
    LAB(clamp(l, -16.0, 100.0), clamp(a, -86.185, 98.254), clamp(b, -107.863, 94.482), clamp(alpha, 0, 1))
}

case class XYZ(x: Double, y: Double, z: Double, alpha: Double = 1.0) extends AbsoluteColor {
  override def toXYZ = this

  override def toRGBA = {
    var XYZ(x, y, z, alpha) = this
    x /= 100
    y /= 100
    z /= 100

    var r = (x * 3.2406) + (y * -1.5372) + (z * -0.4986)
    var g = (x * -0.9689) + (y * 1.8758) + (z * 0.0415)
    var b = (x * 0.0557) + (y * -0.2040) + (z * 1.0570)

    // assume sRGB
    r = if (r > 0.0031308) (1.055 * math.pow(r, 1.0 / 2.4)) - 0.055 else r * 12.92
    g = if (g > 0.0031308) (1.055 * math.pow(g, 1.0 / 2.4)) - 0.055 else g * 12.92
    b = if (b > 0.0031308) (1.055 * math.pow(b, 1.0 / 2.4)) - 0.055 else b * 12.92

    r = math.min(math.max(0, r), 1)
    g = math.min(math.max(0, g), 1)
    b = math.min(math.max(0, b), 1)

    Colors.rgba(r, g, b, alpha)
  }
}

case class HSL(h: Double, s: Double, l: Double, alpha: Double = 1.0) extends AbsoluteColor {
  override def toRGBA = {
    val c = (1 - math.abs(2 * l - 1)) * s
    val x = c * (1 - math.abs((h * 6) % 2 - 1))
    val m = l - c / 2
    val (r, g, b) =
      if (h < 60 / 360.0) (c, x, 0.0)
      else if (h < 120 / 360.0) (x, c, 0.0)
      else if (h < 180 / 360.0) (0.0, c, x)
      else if (h < 240 / 360.0) (0.0, x, c)
      else if (h < 300 / 360.0) (x, 0.0, c)
      else (c, 0.0, x)
    Colors.rgba(r + m, g + m, b + m, alpha)
  }

  override def toXYZ = toRGBA.toXYZ
}

sealed trait PaletteVariant
case object ColorRegular                  extends PaletteVariant
case object ColorLight                    extends PaletteVariant
case object ColorDark                     extends PaletteVariant
case class ColorLightness(lightness: Int) extends PaletteVariant

case class PaletteRef(idx: Int, variant: PaletteVariant = ColorRegular) extends Color {
  def light                 = PaletteRef(idx, ColorLight)
  def lighter               = PaletteRef(idx, ColorLightness(25))
  def lightest              = PaletteRef(idx, ColorLightness(75))
  def dark                  = PaletteRef(idx, ColorDark)
  def darker                = PaletteRef(idx, ColorLightness(-25))
  def darkest               = PaletteRef(idx, ColorLightness(-75))
  def lightness(value: Int) = PaletteRef(idx, ColorLightness(value min 100 max -100))
}

case class PaletteColor(color: AbsoluteColor, textColor: AbsoluteColor)

case class PaletteEntry(color: PaletteColor, light: PaletteColor, dark: PaletteColor) {
  def variant(v: PaletteVariant): AbsoluteColor = v match {
    case ColorRegular => color.color
    case ColorLight   => light.color
    case ColorDark    => dark.color
    case ColorLightness(value) =>
      if (value >= 0)
        color.color.interpolate(light.color, value / 50.0)
      else
        color.color.interpolate(dark.color, -value / 50.0)
  }
}

object PaletteEntry {
  def textColorOn(color: LAB): AbsoluteColor = {
    if (color.l > 50)
      RGB.validated(0.0, 0.0, 0.0, 0.87)
    else
      RGB.validated(1.0, 1.0, 1.0, 0.95)
  }

  def apply(color: AbsoluteColor): PaletteEntry = {
    // calculate light and dark variants
    val normal = color.toLAB
    val light  = normal.brightness(+20)
    val dark   = normal.brightness(-20)
    PaletteEntry(
      PaletteColor(normal, textColorOn(normal)),
      PaletteColor(light, textColorOn(light)),
      PaletteColor(dark, textColorOn(dark))
    )
  }
}
case class Palette(entries: Array[PaletteEntry]) {
  def apply(idx: Int): PaletteEntry = {
    if (idx < 0 || idx >= entries.length)
      Palette.failureColor
    else
      entries(idx)
  }
}

object Palette {
  val failureColor = PaletteEntry(
    PaletteColor(Colors.fuchsia, Colors.black),
    PaletteColor(Colors.fuchsia, Colors.black),
    PaletteColor(Colors.fuchsia, Colors.black)
  )

  final val Base       = 0
  final val Primary    = Base + 1
  final val Secondary  = Primary + 1
  final val Tertiary   = Secondary + 1
  final val Error      = Tertiary + 1
  final val Warning    = Error + 1
  final val Success    = Warning + 1
  final val UserColors = 16

  def userColor(idx: Int) = idx + UserColors

  def empty: Palette = Palette(Array.fill(UserColors)(failureColor))

  def apply(
      base: PaletteEntry,
      primary: PaletteEntry,
      secondary: PaletteEntry,
      tertiary: PaletteEntry,
      error: PaletteEntry,
      warning: PaletteEntry,
      success: PaletteEntry,
      userDefined: Seq[PaletteEntry] = Nil
  ): Palette = {
    val entries = Array.ofDim[PaletteEntry](UserColors + userDefined.size)
    entries(Base) = base
    entries(Primary) = primary
    entries(Secondary) = secondary
    entries(Tertiary) = tertiary
    entries(Error) = error
    entries(Warning) = warning
    entries(Success) = success
    userDefined.zipWithIndex.foreach { case (e, i) => entries(UserColors + i) = e }
    Palette(entries)
  }
}

object Color {
  import boopickle.Default._
  implicit val paletteVariantPickler = compositePickler[PaletteVariant]
    .addConcreteType[ColorRegular.type]
    .addConcreteType[ColorLight.type]
    .addConcreteType[ColorDark.type]
    .addConcreteType[ColorLightness]

  implicit val rgbColorPickler = compositePickler[RGBColor]
    .addConcreteType[RGB]
    .addConcreteType[RGBAlpha]

  implicit val absoluteColorPickler = compositePickler[AbsoluteColor]
    .join(rgbColorPickler)
    .addConcreteType[HSL]
    .addConcreteType[LAB]
    .addConcreteType[XYZ]

  implicit val colorPickler = compositePickler[Color]
    .join(absoluteColorPickler)
    .addConcreteType[PaletteRef]

  implicit val palettePickler = generatePickler[Palette]
}

trait ColorProvider {
  def getColor(idx: Int): PaletteEntry
}

trait Colors {
  @inline def rgb(r: Int, g: Int, b: Int): RGB =
    RGB(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF))

  @inline def rgba(r: Int, g: Int, b: Int, alpha: Double): RGBAlpha =
    RGBAlpha(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF), alpha)

  def rgba(r: Double, g: Double, b: Double, alpha: Double): RGBAlpha =
    rgba(math.round(r * 255).toInt, math.round(g * 255).toInt, math.round(b * 255).toInt, alpha)

  @inline def hsla(h: Double, s: Double, l: Double, a: Double): HSL =
    HSL(h, s, l, a)

  implicit def int2color(i: Int): RGB = RGB(i & 0xFFFFFF)
}

object Colors extends Colors {
  val base      = PaletteRef(Palette.Base)
  val primary   = PaletteRef(Palette.Primary)
  val secondary = PaletteRef(Palette.Secondary)
  val tertiary  = PaletteRef(Palette.Tertiary)
  val error     = PaletteRef(Palette.Error)
  val warning   = PaletteRef(Palette.Warning)
  val success   = PaletteRef(Palette.Success)

  def fromPalette(idx: Int) = PaletteRef(idx)
  def alpha(a: Double)      = RGBAlpha(0, a)

  val aliceblue            = RGB(0xF0F8FF)
  val antiquewhite         = RGB(0xFAEBD7)
  val aqua                 = RGB(0x00FFFF)
  val aquamarine           = RGB(0x7FFFD4)
  val azure                = RGB(0xF0FFFF)
  val beige                = RGB(0xF5F5DC)
  val bisque               = RGB(0xFFE4C4)
  val black                = RGB(0x000000)
  val blanchedalmond       = RGB(0xFFEBCD)
  val blue                 = RGB(0x0000FF)
  val blueviolet           = RGB(0x8A2BE2)
  val brown                = RGB(0xA52A2A)
  val burlywood            = RGB(0xDEB887)
  val cadetblue            = RGB(0x5F9EA0)
  val chartreuse           = RGB(0x7FFF00)
  val chocolate            = RGB(0xD2691E)
  val coral                = RGB(0xFF7F50)
  val cornflowerblue       = RGB(0x6495ED)
  val cornsilk             = RGB(0xFFF8DC)
  val crimson              = RGB(0xDC143C)
  val cyan                 = RGB(0x00FFFF)
  val darkblue             = RGB(0x00008B)
  val darkcyan             = RGB(0x008B8B)
  val darkgoldenrod        = RGB(0xB8860B)
  val darkgray             = RGB(0xA9A9A9)
  val darkgreen            = RGB(0x006400)
  val darkkhaki            = RGB(0xBDB76B)
  val darkmagenta          = RGB(0x8B008B)
  val darkolivegreen       = RGB(0x556B2F)
  val darkorange           = RGB(0xFF8C00)
  val darkorchid           = RGB(0x9932CC)
  val darkred              = RGB(0x8B0000)
  val darksalmon           = RGB(0xE9967A)
  val darkseagreen         = RGB(0x8FBC8F)
  val darkslateblue        = RGB(0x483D8B)
  val darkslategray        = RGB(0x2F4F4F)
  val darkturquoise        = RGB(0x00CED1)
  val darkviolet           = RGB(0x9400D3)
  val deeppink             = RGB(0xFF1493)
  val deepskyblue          = RGB(0x00BFFF)
  val dimgray              = RGB(0x696969)
  val dodgerblue           = RGB(0x1E90FF)
  val firebrick            = RGB(0xB22222)
  val floralwhite          = RGB(0xFFFAF0)
  val forestgreen          = RGB(0x228B22)
  val fuchsia              = RGB(0xFF00FF)
  val gainsboro            = RGB(0xDCDCDC)
  val ghostwhite           = RGB(0xF8F8FF)
  val gold                 = RGB(0xFFD700)
  val goldenrod            = RGB(0xDAA520)
  val gray                 = RGB(0x7F7F7F)
  val green                = RGB(0x008000)
  val greenyellow          = RGB(0xADFF2F)
  val honeydew             = RGB(0xF0FFF0)
  val hotpink              = RGB(0xFF69B4)
  val indianred            = RGB(0xCD5C5C)
  val indigo               = RGB(0x4B0082)
  val ivory                = RGB(0xFFFFF0)
  val khaki                = RGB(0xF0E68C)
  val lavender             = RGB(0xE6E6FA)
  val lavenderblush        = RGB(0xFFF0F5)
  val lawngreen            = RGB(0x7CFC00)
  val lemonchiffon         = RGB(0xFFFACD)
  val lightblue            = RGB(0xADD8E6)
  val lightcoral           = RGB(0xF08080)
  val lightcyan            = RGB(0xE0FFFF)
  val lightgoldenrodyellow = RGB(0xFAFAD2)
  val lightgreen           = RGB(0x90EE90)
  val lightgrey            = RGB(0xD3D3D3)
  val lightpink            = RGB(0xFFB6C1)
  val lightsalmon          = RGB(0xFFA07A)
  val lightseagreen        = RGB(0x20B2AA)
  val lightskyblue         = RGB(0x87CEFA)
  val lightslategray       = RGB(0x778899)
  val lightsteelblue       = RGB(0xB0C4DE)
  val lightyellow          = RGB(0xFFFFE0)
  val lime                 = RGB(0x00FF00)
  val limegreen            = RGB(0x32CD32)
  val linen                = RGB(0xFAF0E6)
  val magenta              = RGB(0xFF00FF)
  val maroon               = RGB(0x800000)
  val mediumaquamarine     = RGB(0x66CDAA)
  val mediumblue           = RGB(0x0000CD)
  val mediumorchid         = RGB(0xBA55D3)
  val mediumpurple         = RGB(0x9370DB)
  val mediumseagreen       = RGB(0x3CB371)
  val mediumslateblue      = RGB(0x7B68EE)
  val mediumspringgreen    = RGB(0x00FA9A)
  val mediumturquoise      = RGB(0x48D1CC)
  val mediumvioletred      = RGB(0xC71585)
  val midnightblue         = RGB(0x191970)
  val mintcream            = RGB(0xF5FFFA)
  val mistyrose            = RGB(0xFFE4E1)
  val moccasin             = RGB(0xFFE4B5)
  val navajowhite          = RGB(0xFFDEAD)
  val navy                 = RGB(0x000080)
  val navyblue             = RGB(0x9FAFDF)
  val oldlace              = RGB(0xFDF5E6)
  val olive                = RGB(0x808000)
  val olivedrab            = RGB(0x6B8E23)
  val orange               = RGB(0xFFA500)
  val orangered            = RGB(0xFF4500)
  val orchid               = RGB(0xDA70D6)
  val palegoldenrod        = RGB(0xEEE8AA)
  val palegreen            = RGB(0x98FB98)
  val paleturquoise        = RGB(0xAFEEEE)
  val palevioletred        = RGB(0xDB7093)
  val papayawhip           = RGB(0xFFEFD5)
  val peachpuff            = RGB(0xFFDAB9)
  val peru                 = RGB(0xCD853F)
  val pink                 = RGB(0xFFC0CB)
  val plum                 = RGB(0xDDA0DD)
  val powderblue           = RGB(0xB0E0E6)
  val purple               = RGB(0x800080)
  val red                  = RGB(0xFF0000)
  val rosybrown            = RGB(0xBC8F8F)
  val royalblue            = RGB(0x4169E1)
  val saddlebrown          = RGB(0x8B4513)
  val salmon               = RGB(0xFA8072)
  val sandybrown           = RGB(0xFA8072)
  val seagreen             = RGB(0x2E8B57)
  val seashell             = RGB(0xFFF5EE)
  val sienna               = RGB(0xA0522D)
  val silver               = RGB(0xC0C0C0)
  val skyblue              = RGB(0x87CEEB)
  val slateblue            = RGB(0x6A5ACD)
  val slategray            = RGB(0x708090)
  val snow                 = RGB(0xFFFAFA)
  val springgreen          = RGB(0x00FF7F)
  val steelblue            = RGB(0x4682B4)
  val tan                  = RGB(0xD2B48C)
  val teal                 = RGB(0x008080)
  val thistle              = RGB(0xD8BFD8)
  val tomato               = RGB(0xFF6347)
  val turquoise            = RGB(0x40E0D0)
  val violet               = RGB(0xEE82EE)
  val wheat                = RGB(0xF5DEB3)
  val white                = RGB(0xFFFFFF)
  val whitesmoke           = RGB(0xF5F5F5)
  val yellow               = RGB(0xFFFF00)
  val yellowgreen          = RGB(0x9ACD32)
}
