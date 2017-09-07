package suzaku.widget

import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetProtocolProvider, WidgetProxy}

object TextField extends WidgetProtocolProvider {
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

  override def widgetProtocol = TextFieldProtocol

  def apply(value: String) = WBlueprint(value)

  def apply(value: String, onChange: String => Unit) = WBlueprint(value, Some(onChange))
}
