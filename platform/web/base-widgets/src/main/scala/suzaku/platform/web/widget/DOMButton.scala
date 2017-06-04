package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{WidgetBuilder, WidgetManager}
import suzaku.widget.ButtonProtocol
import org.scalajs.dom

class DOMButton(widgetId: Int, context: ButtonProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[ButtonProtocol.type, dom.html.Button](widgetId, widgetManager) {
  import ButtonProtocol._

  val artifact = {
    import scalatags.JsDom.all._
    DOMWidgetArtifact(button(context.label, onclick := onClick _).render)
  }

  def onClick(e: dom.MouseEvent): Unit = {
    channel.send(Click)
  }

  override def process = {
    case SetLabel(label) =>
      modifyDOM(node => node.replaceChild(textNode(label), node.firstChild))
    case msg =>
      super.process(msg)
  }
}

class DOMButtonBuilder(widgetManager: WidgetManager) extends WidgetBuilder(ButtonProtocol) {
  import ButtonProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMButton(widgetId, context, widgetManager)
}
