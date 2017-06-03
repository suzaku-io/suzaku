package suzaku.platform.web.widget

import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.WidgetBuilder
import suzaku.widget.ListViewProtocol
import org.scalajs.dom

class DOMListView(context: ListViewProtocol.ChannelContext) extends DOMWidget[ListViewProtocol.type, dom.html.Div] {
  import ListViewProtocol._

  val artifact = {
    import scalatags.JsDom.all._
    DOMWidgetArtifact(div.render)
  }

  override def setChildren(children: Seq[W]) = {
    import org.scalajs.dom.ext._
    modifyDOM { el =>
      el.childNodes.foreach(el.removeChild)
      children.foreach { c =>
        el.appendChild(c.artifact.el)
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

object DOMListViewBuilder extends WidgetBuilder(ListViewProtocol) {
  import ListViewProtocol._
  override protected def create(context: ChannelContext) =
    new DOMListView(context)
}
