package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMUIManager, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, UIManager}
import suzaku.widget.TextFieldProtocol
import org.scalajs.dom

class DOMTextField(widgetId: Int, context: TextFieldProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidget[TextFieldProtocol.type, dom.html.Input](widgetId, widgetManager) {
  import TextFieldProtocol._

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

class DOMTextFieldBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TextFieldProtocol) {
  import TextFieldProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTextField(widgetId, context, widgetManager)
}
