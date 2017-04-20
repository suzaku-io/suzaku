package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.WidgetBuilder
import org.scalajs.dom
import suzaku.widget.TextProtocol

class DOMText(context: TextProtocol.ChannelContext) extends DOMWidget[TextProtocol.type, dom.Text] {
  import TextProtocol._

  val artifact = {
    DOMWidgetArtifact(textNode(context.text))
  }

  override def process = {
    case SetText(text) =>
      modifyDOM(e => e.data = text)
  }
}

object DOMTextBuilder extends WidgetBuilder(TextProtocol) {
  import TextProtocol._

  override protected def create(context: ChannelContext) =
    new DOMText(context)
}
