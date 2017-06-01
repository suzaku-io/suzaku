package suzaku.ui.style

import scala.language.implicitConversions

sealed trait Length {
  val value: Double
  val unit: String

  override def toString: String = value.toString + unit
}

sealed trait LengthUnit extends Length

final case class LengthU(value: Double) extends Length { val unit = "" }

final case class LengthPx(value: Double) extends LengthUnit { val unit = "px" }

final case class LengthPct(value: Double) extends LengthUnit { val unit = "%" }

final case class LengthEm(value: Double) extends LengthUnit { val unit = "em" }

final case class LengthRem(value: Double) extends LengthUnit { val unit = "rem" }

final case class LengthVw(value: Double) extends LengthUnit { val unit = "vw" }

final case class LengthVh(value: Double) extends LengthUnit { val unit = "vh" }

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

  implicit def int2u(v: Int): LengthU = LengthU(v)
}
