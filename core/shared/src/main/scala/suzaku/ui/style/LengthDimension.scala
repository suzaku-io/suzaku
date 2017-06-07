package suzaku.ui.style

import scala.language.implicitConversions

sealed trait LengthDimension

sealed trait LengthUnit extends LengthDimension {
  val value: Double
  val unit: String

  override def toString: String = value.toString + unit
}

final case class LengthU(value: Double) extends LengthDimension {
  override def toString: String = value.toString
}

final case class LengthPx(value: Double) extends LengthUnit { val unit = "px" }

final case class LengthPct(value: Double) extends LengthUnit { val unit = "%" }

final case class LengthEm(value: Double) extends LengthUnit { val unit = "em" }

final case class LengthRem(value: Double) extends LengthUnit { val unit = "rem" }

final case class LengthVw(value: Double) extends LengthUnit { val unit = "vw" }

final case class LengthVh(value: Double) extends LengthUnit { val unit = "vh" }

case object LengthAuto extends LengthUnit {
  val value                     = 0
  val unit                      = ""
  override def toString: String = "auto"
}

sealed trait WidthDimension

case object WidthThin                 extends WidthDimension
case object WidthMedium               extends WidthDimension
case object WidthThick                extends WidthDimension
case class WidthLength(w: LengthUnit) extends WidthDimension

sealed trait FontDimension

case object FontXXSmall                      extends FontDimension
case object FontXSmall                       extends FontDimension
case object FontSmall                        extends FontDimension
case object FontSmaller                      extends FontDimension
case object FontMedium                       extends FontDimension
case object FontLarge                        extends FontDimension
case object FontLarger                       extends FontDimension
case object FontXLarge                       extends FontDimension
case object FontXXLarge                      extends FontDimension
case class FontLength(size: LengthDimension) extends FontDimension

sealed trait WeightDimension

case object WeightNormal            extends WeightDimension
case object WeightBold              extends WeightDimension
case object WeightBolder            extends WeightDimension
case object WeightLighter           extends WeightDimension
case class WeightValue(weight: Int) extends WeightDimension

trait LengthImplicits {
  implicit class int2length(v: Int) {
    def px  = LengthPx(v)
    def %%  = LengthPct(v)
    def em  = LengthEm(v)
    def rem = LengthRem(v)
    def vw  = LengthVw(v)
    def vh  = LengthVh(v)
  }

  implicit class double2length(v: Double) {
    def px  = LengthPx(v)
    def %%  = LengthPct(v)
    def em  = LengthEm(v)
    def rem = LengthRem(v)
    def vw  = LengthVw(v)
    def vh  = LengthVh(v)
  }

  implicit def int2u(v: Int): LengthU                        = LengthU(v)
  implicit def autoLength(a: Keywords.auto.type): LengthUnit = LengthAuto

  implicit def thin2width(a: Keywords.thin.type): WidthDimension     = WidthThin
  implicit def medium2width(a: Keywords.medium.type): WidthDimension = WidthMedium
  implicit def thick2width(a: Keywords.thick.type): WidthDimension   = WidthThick
  implicit def length2width(l: LengthUnit): WidthDimension           = WidthLength(l)

  implicit def length2font(l: LengthUnit): FontDimension                = FontLength(l)
  implicit def xxsmall2width(a: Keywords.xxsmall.type): FontDimension   = FontXXSmall
  implicit def xsmall2width(a: Keywords.xsmall.type): FontDimension     = FontXSmall
  implicit def small2width(a: Keywords.small.type): FontDimension       = FontSmall
  implicit def smaller2width(a: Keywords.smaller.type): FontDimension   = FontSmaller
  implicit def xxlarge2width(a: Keywords.xxlarge.type): FontDimension   = FontXXLarge
  implicit def xlarge2width(a: Keywords.xlarge.type): FontDimension     = FontXLarge
  implicit def large2width(a: Keywords.large.type): FontDimension       = FontLarge
  implicit def larger2width(a: Keywords.larger.type): FontDimension     = FontLarger
  implicit def medium2fontwidth(a: Keywords.medium.type): FontDimension = FontMedium

  implicit def int2weight(w: Int): WeightDimension = WeightValue(w)
}
