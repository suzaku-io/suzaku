package suzaku.widget

import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object Checkbox extends WidgetProtocolProvider {
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

  override def widgetProtocol = CheckboxProtocol

  def apply(checked: Boolean) = WBlueprint(checked, None, None)

  def apply(checked: Boolean, label: String) = WBlueprint(checked, Some(label), None)

  def apply(checked: Boolean, onChange: Boolean => Unit) = WBlueprint(checked, None, Some(onChange))

  def apply(checked: Boolean, label: String, onChange: Boolean => Unit) = WBlueprint(checked, Some(label), Some(onChange))
}
