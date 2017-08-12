package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMUIManager, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, UIManager}
import suzaku.widget.CheckboxProtocol
import org.scalajs.dom

class DOMCheckbox(widgetId: Int, context: CheckboxProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidget[CheckboxProtocol.type, dom.html.Input](widgetId, widgetManager) {
  import CheckboxProtocol._

  val artifact = {
    val el = tag[dom.html.Input]("input")
    el.addEventListener("click", onChange _)
    el.setAttribute("type", "checkbox")
    el.checked = context.value
    DOMWidgetArtifact(el)
  }

  def onChange(e: dom.Event): Unit = {
    channel.send(ValueChanged(artifact.el.checked))
  }

  override def process = {
    case SetValue(value) =>
      modifyDOM(node => node.checked = value)
    case msg =>
      super.process(msg)
  }
}

class DOMCheckboxBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(CheckboxProtocol) {
  import CheckboxProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMCheckbox(widgetId, context, widgetManager)
}
