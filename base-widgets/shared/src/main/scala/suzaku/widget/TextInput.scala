package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetProxy}

object TextInputProtocol extends Protocol {

  sealed trait TextInputMessage extends Message

  case class SetValue(value: String) extends TextInputMessage

  case class ValueChanged(value: String) extends TextInputMessage

  val mPickler = compositePickler[TextInputMessage]
    .addConcreteType[SetValue]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg) = defineProtocol(mPickler)

  case class ChannelContext(initialValue: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object TextInput {
  class WProxy private[TextInput] (bd: WBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TextInputProtocol, bd, viewId, uiChannel) {
    import TextInputProtocol._

    override def process = {
      case ValueChanged(value) =>
        blueprint.onChange.foreach(_(value))
      case message =>
        super.process(message)
    }

    override protected def initView = ChannelContext(bd.value)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.value != blueprint.value)
        send(SetValue(newBlueprint.value))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TextInput] (value: String, onChange: Option[String => Unit] = None)
      extends WidgetBlueprint {
    type P     = TextInputProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new Proxy(this)(viewId, uiChannel)
  }

  def apply(value: String) = WBlueprint(value)

  def apply(value: String, onChange: String => Unit) = WBlueprint(value, Some(onChange))
}
