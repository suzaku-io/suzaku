package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, WidgetManager}
import org.scalajs.dom
import suzaku.widget.TextProtocol

class DOMText(widgetId: Int, context: TextProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[TextProtocol.type, dom.Text](widgetId, widgetManager) {
  import TextProtocol._

  val artifact = {
    DOMWidgetArtifact(textNode(context.text))
  }

  override def process = {
    case SetText(text) =>
      modifyDOM(e => e.data = text)
    case msg =>
      super.process(msg)
  }
}

class DOMTextBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TextProtocol) {
  import TextProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMText(widgetId, context, widgetManager)
}
