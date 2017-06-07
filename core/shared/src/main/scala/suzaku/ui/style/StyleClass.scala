package suzaku.ui.style

import boopickle.{PickleState, Pickler, UnpickleState}

// Style identifiers
abstract class StyleClass {
  def style: List[StyleDef]

  // register at initialization time
  val id = StyleRegistry.register(style)
}

case class PureStyleClass(override val id: Int) extends StyleClass {
  def style: List[StyleDef] = Nil
}

object StyleClassPickler extends Pickler[StyleClass] {
  override def pickle(obj: StyleClass)(implicit state: PickleState): Unit = {
    state.enc.writeInt(obj.id)
  }

  override def unpickle(implicit state: UnpickleState): StyleClass = {
    PureStyleClass(state.dec.readInt)
  }
}
