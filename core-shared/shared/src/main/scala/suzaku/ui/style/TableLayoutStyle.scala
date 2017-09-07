package suzaku.ui.style

import suzaku.ui.Keywords

sealed trait TableLayoutStyle

case object TableLayoutAuto extends TableLayoutStyle

case object TableLayoutFixed extends TableLayoutStyle

object TableLayoutStyle {
  import boopickle.Default._

  implicit val tableLayoutPickler = compositePickler[TableLayoutStyle]
    .addConcreteType[TableLayoutAuto.type]
    .addConcreteType[TableLayoutFixed.type]
}

trait TableLayoutStyleImplicits {
  import scala.language.implicitConversions

  implicit def auto2Style(a: Keywords.auto.type): TableLayoutStyle   = TableLayoutAuto
  implicit def fixed2Style(a: Keywords.fixed.type): TableLayoutStyle = TableLayoutFixed
}
