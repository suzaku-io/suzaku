package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.WidgetBuilder
import suzaku.widget.TextInputProtocol
import org.scalajs.dom

class DOMTextInput(context: TextInputProtocol.ChannelContext) extends DOMWidget[TextInputProtocol.type, dom.html.Input] {
  import TextInputProtocol._

  val artifact = {
    import scalatags.JsDom.all._

    val node = input(tpe := "text", oninput := onChange _).render
    node.value = context.initialValue
    DOMWidgetArtifact(node)
  }

  private def onChange(e: dom.Event): Unit = {
    channel.send(ValueChanged(artifact.el.value))
  }

  override def process = {
    case SetValue(text) =>
      modifyDOM(node => node.value = text)
  }

}

object DOMTextInputBuilder extends WidgetBuilder(TextInputProtocol) {
  import TextInputProtocol._

  override protected def create(context: ChannelContext) = new DOMTextInput(context)
}
