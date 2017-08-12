package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMUIManager, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, UIManager}
import org.scalajs.dom
import suzaku.widget.TextProtocol

class DOMText(widgetId: Int, context: TextProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidget[TextProtocol.type, dom.html.Span](widgetId, widgetManager) {
  import TextProtocol._

  val innerText = textNode(context.text)

  val artifact = {
    val span = dom.document.createElement("span").asInstanceOf[dom.html.Span]
    span.appendChild(innerText)
    DOMWidgetArtifact(span)
  }

  override def process = {
    case SetText(text) =>
      modifyDOM(_ => innerText.data = text)
    case msg =>
      super.process(msg)
  }
}

class DOMTextBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TextProtocol) {
  import TextProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMText(widgetId, context, widgetManager)
}
