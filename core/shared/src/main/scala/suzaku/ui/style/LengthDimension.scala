package suzaku.ui.style

import suzaku.ui.Keywords

import scala.language.implicitConversions

sealed trait LengthDimension

sealed trait LengthUnit extends LengthDimension

case class LengthU(value: Double)   extends LengthDimension
case class LengthPx(value: Double)  extends LengthUnit
case class LengthPct(value: Double) extends LengthUnit
case class LengthEm(value: Double)  extends LengthUnit
case class LengthRem(value: Double) extends LengthUnit
case class LengthVw(value: Double)  extends LengthUnit
case class LengthVh(value: Double)  extends LengthUnit
case class LengthFr(value: Int)     extends LengthUnit
case object LengthAuto              extends LengthUnit

object LengthDimension {
  import boopickle.Default._
  implicit val lengthPickler = compositePickler[LengthUnit]
    .addTransform[LengthPx, Double](_.value, LengthPx)
    .addTransform[LengthPct, Double](_.value, LengthPct)
    .addTransform[LengthEm, Double](_.value, LengthEm)
    .addTransform[LengthRem, Double](_.value, LengthRem)
    .addTransform[LengthVw, Double](_.value, LengthVw)
    .addTransform[LengthVh, Double](_.value, LengthVh)
    .addTransform[LengthFr, Int](_.value, LengthFr)
    .addConcreteType[LengthAuto.type]

  implicit val lengthDimensionPickler = compositePickler[LengthDimension]
    .join(lengthPickler)
    .addTransform[LengthU, Double](_.value, LengthU)
}

sealed trait WidthDimension

case object WidthThin                 extends WidthDimension
case object WidthMedium               extends WidthDimension
case object WidthThick                extends WidthDimension
case class WidthLength(w: LengthUnit) extends WidthDimension

object WidthDimension {
  import boopickle.Default._
  import LengthDimension._
  implicit val widthPickler = compositePickler[WidthDimension]
    .addConcreteType[WidthThin.type]
    .addConcreteType[WidthMedium.type]
    .addConcreteType[WidthThick.type]
    .addConcreteType[WidthLength]
}

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

object FontDimension {
  import boopickle.Default._
  import LengthDimension._
  implicit val fontPickler = compositePickler[FontDimension]
    .addConcreteType[FontXXSmall.type]
    .addConcreteType[FontXSmall.type]
    .addConcreteType[FontSmall.type]
    .addConcreteType[FontSmaller.type]
    .addConcreteType[FontMedium.type]
    .addConcreteType[FontLarge.type]
    .addConcreteType[FontLarger.type]
    .addConcreteType[FontXLarge.type]
    .addConcreteType[FontXXLarge.type]
    .addConcreteType[FontLength]
}

sealed trait WeightDimension

case object WeightNormal            extends WeightDimension
case object WeightBold              extends WeightDimension
case object WeightBolder            extends WeightDimension
case object WeightLighter           extends WeightDimension
case class WeightValue(weight: Int) extends WeightDimension

object WeightDimension {
  import boopickle.Default._
  implicit val weightPickler = compositePickler[WeightDimension]
    .addConcreteType[WeightNormal.type]
    .addConcreteType[WeightBold.type]
    .addConcreteType[WeightBolder.type]
    .addConcreteType[WeightLighter.type]
    .addConcreteType[WeightValue]
}

trait LengthImplicits {
  implicit class int2length(v: Int) {
    def px  = LengthPx(v)
    def %%  = LengthPct(v)
    def em  = LengthEm(v)
    def rem = LengthRem(v)
    def vw  = LengthVw(v)
    def vh  = LengthVh(v)
    def fr  = LengthFr(v)
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
