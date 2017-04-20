package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetProxy}

object ButtonProtocol extends Protocol {

  sealed trait ButtonMessage extends Message

  case class SetLabel(label: String) extends ButtonMessage

  case object Click extends ButtonMessage

  val bmPickler = compositePickler[ButtonMessage]
    .addConcreteType[SetLabel]
    .addConcreteType[Click.type]

  implicit val (messagePickler, witnessMsg) = defineProtocol(bmPickler)

  case class ChannelContext(label: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object Button {
  class ButtonProxy private[Button] (bd: ButtonBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(ButtonProtocol, bd, viewId, uiChannel) {
    import ButtonProtocol._

    override def process = {
      case Click =>
        blueprint.onClick.foreach(f => f())
      case message =>
        super.process(message)
    }

    override def initView = ChannelContext(bd.label)

    override def update(newBlueprint: ButtonBlueprint) = {
      if (newBlueprint.label != blueprint.label)
        send(SetLabel(newBlueprint.label))
      super.update(newBlueprint)
    }
  }

  case class ButtonBlueprint private[Button] (label: String, onClick: Option[() => Unit] = None) extends WidgetBlueprint {
    type P     = ButtonProtocol.type
    type Proxy = ButtonProxy
    type This  = ButtonBlueprint

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new ButtonProxy(this)(viewId, uiChannel)
  }

  def apply(label: String) = ButtonBlueprint(label, None)

  def apply(label: String, onClick: () => Unit) = ButtonBlueprint(label, Some(onClick))
}
