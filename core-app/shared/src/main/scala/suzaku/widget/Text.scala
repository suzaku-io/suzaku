package suzaku.widget

import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{WidgetBlueprint, WidgetProxy}

object Text {

  case class TextBlueprint(text: String) extends WidgetBlueprint {
    type P     = TextProtocol.type
    type Proxy = TextProxy
    type This  = TextBlueprint

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new TextProxy(this)(widgetId, uiChannel)
  }

  class TextProxy private[Text] (td: TextBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TextProtocol, td, widgetId, uiChannel) {
    import TextProtocol._

    override def initWidget = ChannelContext(td.text)

    override def update(newBlueprint: TextBlueprint) = {
      if (newBlueprint.text != blueprint.text)
        send(SetText(newBlueprint.text))
      super.update(newBlueprint)
    }
  }

  def apply(text: String) = TextBlueprint(text)
}
