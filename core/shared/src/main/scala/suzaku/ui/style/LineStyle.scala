package suzaku.ui.style

import suzaku.ui.Keywords

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

object LineStyle {
  import boopickle.Default._
  implicit val lineStylePickler = compositePickler[LineStyle]
    .addConcreteType[LineNone.type]
    .addConcreteType[LineHidden.type]
    .addConcreteType[LineSolid.type]
    .addConcreteType[LineDotted.type]
    .addConcreteType[LineDashed.type]
    .addConcreteType[LineInset.type]
    .addConcreteType[LineOutset.type]
    .addConcreteType[LineDouble.type]
}

trait LineStyleImplicits {
  implicit def none2Style(a: Keywords.none.type): LineStyle     = LineNone
  implicit def hidden2Style(a: Keywords.hidden.type): LineStyle = LineHidden
  implicit def solid2Style(a: Keywords.solid.type): LineStyle   = LineSolid
  implicit def dotted2Style(a: Keywords.dotted.type): LineStyle = LineDotted
  implicit def dashed2Style(a: Keywords.dashed.type): LineStyle = LineDashed
  implicit def inset2Style(a: Keywords.inset.type): LineStyle   = LineInset
  implicit def outset2Style(a: Keywords.outset.type): LineStyle = LineOutset
}
