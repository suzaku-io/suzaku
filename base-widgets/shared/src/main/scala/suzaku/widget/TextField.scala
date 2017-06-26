package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetBlueprintProvider, WidgetProtocol, WidgetProxy}

object TextFieldProtocol extends Protocol {

  sealed trait TextFieldMessage extends Message

  case class SetValue(value: String) extends TextFieldMessage

  case class ValueChanged(value: String) extends TextFieldMessage

  val mPickler = compositePickler[TextFieldMessage]
    .addConcreteType[SetValue]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(initialValue: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object TextField extends WidgetBlueprintProvider {
  class WProxy private[TextField] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TextFieldProtocol, bd, widgetId, uiChannel) {
    import TextFieldProtocol._

    override def process = {
      case ValueChanged(value) =>
        blueprint.onChange.foreach(_(value))
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext(bd.value)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.value != blueprint.value)
        send(SetValue(newBlueprint.value))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TextField] (value: String, onChange: Option[String => Unit] = None)
      extends WidgetBlueprint {
    type P     = TextFieldProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(value: String) = WBlueprint(value)

  def apply(value: String, onChange: String => Unit) = WBlueprint(value, Some(onChange))
}
