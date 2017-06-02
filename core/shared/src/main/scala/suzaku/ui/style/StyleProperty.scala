package suzaku.ui.style

import boopickle.Default._
import boopickle.{PickleState, Pickler, UnpickleState}

sealed trait StyleProperty

case object EmptyStyle extends StyleProperty

case class Color(color: RGBColor) extends StyleProperty

case class BackgroundColor(color: RGBColor) extends StyleProperty

// Layout related styles
case class Order(order: Int) extends StyleProperty

case class Width(value: LengthUnit) extends StyleProperty

case class Height(value: LengthUnit) extends StyleProperty

// Style identifiers
abstract class StyleId {
  def style: List[StyleProperty]

  // register at initialization time
  val id = StyleRegistry.register(style)
}

case class PureStyleId(override val id: Int) extends StyleId {
  def style: List[StyleProperty] = Nil
}

object StyleIdPickler extends Pickler[StyleId] {
  override def pickle(obj: StyleId)(implicit state: PickleState): Unit = {
    state.enc.writeInt(obj.id)
  }

  override def unpickle(implicit state: UnpickleState): StyleId = {
    PureStyleId(state.dec.readInt)
  }
}

case class StyleIds(styles: List[StyleId]) extends StyleProperty

object StyleProperty {
  import boopickle.DefaultBasic._
  implicit val styleIdPickler = StyleIdPickler

  val pickler = PicklerGenerator.generatePickler[StyleProperty]
}
