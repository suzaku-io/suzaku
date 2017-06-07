package suzaku.ui.style

import scala.language.implicitConversions

sealed trait LengthDimension

sealed trait LengthUnit extends LengthDimension {
  val value: Double
  val unit: String

  override def toString: String = value.toString + unit
}

final case class LengthU(value: Double) extends LengthDimension

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

case object LengthNone extends LengthUnit {
  val value                     = 0
  val unit                      = ""
  override def toString: String = "none"
}

sealed trait WidthDimension

case object WidthThin extends WidthDimension {
  override def toString: String = "thin"
}

case object WidthMedium extends WidthDimension {
  override def toString: String = "medium"
}

case object WidthThick extends WidthDimension {
  override def toString: String = "thick"
}

case class WidthLength(w: LengthUnit) extends WidthDimension

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

  implicit def int2u(v: Int): LengthU                         = LengthU(v)
  implicit def none2Length(a: Keywords.none.type): LengthUnit = LengthNone
  implicit def autoLength(a: Keywords.auto.type): LengthUnit  = LengthAuto

  implicit def thin2width(a: Keywords.thin.type): WidthDimension     = WidthThin
  implicit def medium2width(a: Keywords.medium.type): WidthDimension = WidthMedium
  implicit def thick2width(a: Keywords.thick.type): WidthDimension   = WidthThick
  implicit def length2width(l: LengthUnit): WidthDimension           = WidthLength(l)
}
