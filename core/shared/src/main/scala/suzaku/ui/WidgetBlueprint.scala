package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.style.{StyleId, StyleProperty, StyleIds}

trait WidgetBlueprint extends Blueprint {
  type P <: Protocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  private[suzaku] var _style = Map.empty[Class[_], StyleProperty]

  def createProxy(viewId: Int, uiChannel: UIChannel): Proxy

  def children: List[Blueprint] = Nil

  def sameAs(that: This): Boolean = equals(that)

  @inline final def <<<(styleProperty: StyleProperty*): this.type = {
    _style ++= styleProperty.map(p => (p.getClass, p))
    this
  }

  @inline final def <<(styleId: StyleId*): this.type = {
    _style += (classOf[StyleIds] -> StyleIds(List(styleId: _*)))
    this
  }
}

object WidgetProtocol extends Protocol {
  implicit val stylePropertyPickler = StyleProperty.pickler

  sealed trait WidgetMessage extends Message

  case class UpdateStyle(params: List[(StyleProperty, Boolean)]) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateStyle]

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}
