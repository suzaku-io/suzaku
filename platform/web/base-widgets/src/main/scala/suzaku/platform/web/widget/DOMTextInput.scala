package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, WidgetManager}
import suzaku.widget.TextInputProtocol
import org.scalajs.dom

class DOMTextInput(widgetId: Int, context: TextInputProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[TextInputProtocol.type, dom.html.Input](widgetId, widgetManager) {
  import TextInputProtocol._

  val artifact = {
    val el = tag[dom.html.Input]("input")
    el.addEventListener("input", onChange _)
    el.setAttribute("type", "text")
    el.value = context.initialValue
    DOMWidgetArtifact(el)
  }

  private def onChange(e: dom.Event): Unit = {
    channel.send(ValueChanged(artifact.el.value))
  }

  override def process = {
    case SetValue(text) =>
      modifyDOM { node =>
        // save cursor/selection
        val start = node.selectionStart
        val end   = node.selectionEnd
        node.value = text
        // restore
        node.setSelectionRange(start, end)
      }
    case msg =>
      super.process(msg)
  }

}

class DOMTextInputBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TextInputProtocol) {
  import TextInputProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTextInput(widgetId, context, widgetManager)
}
