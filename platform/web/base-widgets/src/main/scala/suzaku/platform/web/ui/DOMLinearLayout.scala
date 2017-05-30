package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.{LinearLayoutProtocol, WidgetBuilder}

class DOMLinearLayout(context: LinearLayoutProtocol.ChannelContext)
    extends DOMWidget[LinearLayoutProtocol.type, dom.html.Div] {
  import LinearLayoutProtocol._

  val artifact = {
    import scalatags.JsDom.all._
    DOMWidgetArtifact(div(style := buildStyle(context.direction)).render)
  }

  private def buildStyle(direction: Direction): String = {
    val flexDirection = direction match {
      case Horizontal => "row"
      case Vertical   => "column"
    }
    s"display:flex;flex-direction:$flexDirection"
  }

  override def setChildren(children: Seq[Artifact]) = {
    import org.scalajs.dom.ext._
    modifyDOM { el =>
      el.childNodes.foreach(el.removeChild)
      children.foreach { c =>
        el.appendChild(c.el)
      }
    }
  }

  override def process = {
    case SetDirection(direction) =>
      modifyDOM { _.setAttribute("style", buildStyle(direction)) }
  }
}

object DOMLinearLayoutBuilder extends WidgetBuilder(LinearLayoutProtocol) {
  import LinearLayoutProtocol._

  override protected def create(context: ChannelContext) = {
    new DOMLinearLayout(context)
  }
}
