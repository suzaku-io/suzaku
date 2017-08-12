package suzaku.platform.web.widget

import org.scalajs.dom
import suzaku.platform.web._
import suzaku.ui.WidgetBuilder
import suzaku.widget.ButtonProtocol

class DOMButton(widgetId: Int, context: ButtonProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidget[ButtonProtocol.type, dom.html.Button](widgetId, widgetManager) {
  import ButtonProtocol._

  val artifact = {
    val el = tag[dom.html.Button]("button")
    widgetManager.addListener(ClickEvent)(widgetId, el, onClick)
    val span = tag[dom.html.Span]("span")
    span.appendChild(textNode(context.label))
    context.icon.foreach { image =>
      val img = imageNode(image, Some("1em"), Some("1em"), Some("currentColor"))
      span.appendChild(img)
    }
    el.appendChild(span)
    DOMWidgetArtifact(el)
  }

  def onClick(e: DOMEvent[dom.MouseEvent]): Unit = {
    channel.send(Click)
  }

  override def process = {
    case SetLabel(label) =>
      modifyDOM { node =>
        node.firstChild.replaceChild(textNode(label), node.firstChild.firstChild)
      }
    case SetIcon(icon) =>
    case msg =>
      super.process(msg)
  }

  override def closed(): Unit = {
    widgetManager.removeListener(ClickEvent, widgetId, artifact.el)
  }
}

class DOMButtonBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(ButtonProtocol) {
  import ButtonProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMButton(widgetId, context, widgetManager)
}
