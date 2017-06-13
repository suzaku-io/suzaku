package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, WidgetManager}
import suzaku.widget.CheckboxProtocol
import org.scalajs.dom

class DOMCheckbox(widgetId: Int, context: CheckboxProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[CheckboxProtocol.type, dom.html.Input](widgetId, widgetManager) {
  import CheckboxProtocol._

  val artifact = {
    import scalatags.JsDom.all._
    // the presence of the "checked" attribute makes the input checked, even with a false value
    val checkbox =
      if (context.value) input(`type` := "checkbox", checked := true, onclick := onChange _)
      else input(`type` := "checkbox", onclick := onChange _)
    DOMWidgetArtifact(checkbox.render)
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

class DOMCheckboxBuilder(widgetManager: WidgetManager) extends WidgetBuilder(CheckboxProtocol) {
  import CheckboxProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMCheckbox(widgetId, context, widgetManager)
}
