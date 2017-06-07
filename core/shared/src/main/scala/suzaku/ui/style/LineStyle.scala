package suzaku.ui.style

import scala.language.implicitConversions

sealed trait LineStyle

case object LineNone extends LineStyle

case object LineHidden extends LineStyle

case object LineSolid extends LineStyle

case object LineDotted extends LineStyle

case object LineDashed extends LineStyle

case object LineInset extends LineStyle

case object LineOutset extends LineStyle

case object LineDouble extends LineStyle

trait LineStyleImplicits {
  implicit def none2Style(a: Keywords.none.type): LineStyle     = LineNone
  implicit def hidden2Style(a: Keywords.hidden.type): LineStyle = LineHidden
  implicit def solid2Style(a: Keywords.solid.type): LineStyle   = LineSolid
  implicit def dotted2Style(a: Keywords.dotted.type): LineStyle = LineDotted
  implicit def dashed2Style(a: Keywords.dashed.type): LineStyle = LineDashed
  implicit def inset2Style(a: Keywords.inset.type): LineStyle = LineInset
  implicit def outset2Style(a: Keywords.outset.type): LineStyle = LineOutset
}
