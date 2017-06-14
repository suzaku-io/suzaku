package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{Widget, WidgetBuilder, WidgetManager}
import suzaku.widget.ListViewProtocol
import org.scalajs.dom

class DOMListView(widgetId: Int, context: ListViewProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[ListViewProtocol.type, dom.html.Div](widgetId, widgetManager) {
  import ListViewProtocol._

  val artifact = {
    import scalatags.JsDom.all._
    DOMWidgetArtifact(div.render)
  }

  override def setChildren(children: Seq[Widget]) = {
    import org.scalajs.dom.ext._
    modifyDOM { el =>
      el.childNodes.foreach(el.removeChild)
      children.foreach { c =>
        val widget = c.asInstanceOf[DOMWidget[_, _ <: dom.Node]]
        el.appendChild(widget.artifact.el)
      }
    }
  }

  override def process = {
    case SetDirection(direction) =>
    // ignore for now
    case msg =>
      super.process(msg)
  }
}

class DOMListViewBuilder(widgetManager: WidgetManager) extends WidgetBuilder(ListViewProtocol) {
  import ListViewProtocol._
  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMListView(widgetId, context, widgetManager)
}
