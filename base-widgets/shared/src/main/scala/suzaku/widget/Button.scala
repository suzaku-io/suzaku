package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetProtocol, WidgetProxy}

object ButtonProtocol extends Protocol {

  sealed trait ButtonMessage extends Message

  case class SetLabel(label: String) extends ButtonMessage

  case object Click extends ButtonMessage

  val mPickler = compositePickler[ButtonMessage]
    .addConcreteType[SetLabel]
    .addConcreteType[Click.type]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(label: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object Button {
  class WProxy private[Button] (bd: WBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(ButtonProtocol, bd, viewId, uiChannel) {
    import ButtonProtocol._

    override def process = {
      case Click =>
        blueprint.onClick.foreach(f => f())
      case message =>
        super.process(message)
    }

    override def initView = ChannelContext(bd.label)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.label != blueprint.label)
        send(SetLabel(newBlueprint.label))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Button] (label: String, onClick: Option[() => Unit] = None) extends WidgetBlueprint {
    type P     = ButtonProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new WProxy(this)(viewId, uiChannel)
  }

  def apply(label: String) = WBlueprint(label, None)

  def apply(label: String, onClick: () => Unit) = WBlueprint(label, Some(onClick))
}
