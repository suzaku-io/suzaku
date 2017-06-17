package suzaku.ui.layout

import boopickle.{PickleState, Pickler, UnpickleState}

trait LayoutProperty

abstract class LayoutId {
  val id = LayoutIdRegistry.register(getClass)
}

case class AlignSelf(align: Alignment) extends LayoutProperty
case class LayoutWeight(weight: Int)   extends LayoutProperty
case class LayoutGroupId(id: LayoutId) extends LayoutProperty
case class Order(order: Int)           extends LayoutProperty
case class ZOrder(order: Int)          extends LayoutProperty

case class PureLayoutId(override val id: Int) extends LayoutId

class LayoutIdPickler extends Pickler[LayoutId] {
  override def pickle(obj: LayoutId)(implicit state: PickleState): Unit = {
    state.enc.writeInt(obj.id)
  }

  override def unpickle(implicit state: UnpickleState): LayoutId = {
    PureLayoutId(state.dec.readInt)
  }
}

object LayoutProperty {
  import boopickle.Default._

  implicit val layoutIdPickler = new LayoutIdPickler

  val layoutPickler = compositePickler[LayoutProperty]
    .addConcreteType[Order]
    .addConcreteType[ZOrder]
    .addConcreteType[AlignSelf]
    .addConcreteType[LayoutWeight]
    .addConcreteType[LayoutGroupId]
}
