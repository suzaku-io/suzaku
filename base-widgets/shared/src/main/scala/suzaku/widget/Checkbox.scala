package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object CheckboxProtocol extends Protocol {

  sealed trait CheckboxMessage extends Message

  case class SetValue(value: Boolean) extends CheckboxMessage

  case class ValueChanged(value: Boolean) extends CheckboxMessage

  val mPickler = compositePickler[CheckboxMessage]
    .addConcreteType[SetValue]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(value: Boolean)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object Checkbox extends WidgetBlueprintProvider {
  class WProxy private[Checkbox] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(CheckboxProtocol, bd, widgetId, uiChannel) {
    import CheckboxProtocol._

    override def process = {
      case ValueChanged(value) =>
        blueprint.onChange.foreach(_(value))
      case message =>
        super.process(message)
    }

    override def initWidget = ChannelContext(bd.value)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.value != blueprint.value)
        send(SetValue(newBlueprint.value))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Checkbox] (value: Boolean, onChange: Option[Boolean => Unit] = None)
      extends WidgetBlueprint {
    type P     = CheckboxProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new WProxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(value: Boolean) = WBlueprint(value, None)

  def apply(value: Boolean, onChange: Boolean => Unit) = WBlueprint(value, Some(onChange))
}
