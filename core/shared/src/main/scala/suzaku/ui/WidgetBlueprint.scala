package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel

trait WidgetBlueprint extends Blueprint {
  type P <: Protocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  var _layout = Map.empty[Class[_], LayoutParameter]

  def createProxy(viewId: Int, uiChannel: UIChannel): Proxy

  def children: List[Blueprint] = Nil

  def sameAs(that: This): Boolean = equals(that)

  def <<(layoutParameter: LayoutParameter*): this.type = {
    _layout ++= layoutParameter.map(p => (p.getClass, p))
    this
  }
}

object WidgetProtocol extends Protocol {
  sealed trait WidgetMessage extends Message

  case class UpdateLayout(params: List[(LayoutParameter, Boolean)]) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateLayout]

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}
