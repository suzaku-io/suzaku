package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object CheckboxProtocol extends Protocol {

  sealed trait CheckboxMessage extends Message

  case class SetValue(checked: Boolean) extends CheckboxMessage

  case class SetLabel(label: Option[String]) extends CheckboxMessage

  case class ValueChanged(value: Boolean) extends CheckboxMessage

  val mPickler = compositePickler[CheckboxMessage]
    .addConcreteType[SetValue]
    .addConcreteType[SetLabel]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(checked: Boolean, label: Option[String])

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

    override def initWidget = ChannelContext(bd.checked, bd.label)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.checked != blueprint.checked)
        send(SetValue(newBlueprint.checked))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Checkbox] (checked: Boolean, label: Option[String], onChange: Option[Boolean => Unit] = None)
      extends WidgetBlueprint {
    type P     = CheckboxProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new WProxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(checked: Boolean) = WBlueprint(checked, None, None)

  def apply(checked: Boolean, label: String) = WBlueprint(checked, Some(label), None)

  def apply(checked: Boolean, onChange: Boolean => Unit) = WBlueprint(checked, None, Some(onChange))

  def apply(checked: Boolean, label: String, onChange: Boolean => Unit) = WBlueprint(checked, Some(label), Some(onChange))
}
