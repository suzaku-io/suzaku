package suzaku.ui.layout

import boopickle.{PickleState, Pickler, UnpickleState}

trait LayoutProperty

trait LayoutId {
  def id: Int
}

case class AlignSelf(align: Alignment)   extends LayoutProperty
case class JustifySelf(align: Alignment) extends LayoutProperty
case class LayoutWeight(weight: Int)     extends LayoutProperty
case class LayoutSlotId(id: LayoutId)    extends LayoutProperty
case class Order(order: Int)             extends LayoutProperty
case class ZOrder(order: Int)            extends LayoutProperty

case class PureLayoutId(override val id: Int) extends LayoutId

object LayoutId {

  implicit object LayoutIdPickler extends Pickler[LayoutId] {
    override def pickle(obj: LayoutId)(implicit state: PickleState): Unit = {
      state.enc.writeInt(obj.id)
    }

    override def unpickle(implicit state: UnpickleState): LayoutId = {
      PureLayoutId(state.dec.readInt)
    }
  }
}

case class LayoutIdRegistration(id: Int, className: String)

object LayoutProperty {
  import boopickle.Default._
  import Alignment._

  implicit val layoutIdPickler = LayoutId.LayoutIdPickler

  val layoutPickler = compositePickler[LayoutProperty]
    .addConcreteType[Order]
    .addConcreteType[ZOrder]
    .addConcreteType[AlignSelf]
    .addConcreteType[JustifySelf]
    .addConcreteType[LayoutWeight]
    .addConcreteType[LayoutSlotId]
}
