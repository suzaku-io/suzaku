package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.layout.LinearLayoutProtocol
import suzaku.ui.{WidgetBuilder, WidgetManager}

class DOMLinearLayout(widgetId: Int, context: LinearLayoutProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[LinearLayoutProtocol.type, dom.html.Div](widgetId, widgetManager) {
  import LinearLayoutProtocol._
  import Direction._
  import Justify._

  val artifact = {
    import scalatags.JsDom.all._
    val el = div().render
    el.style.setProperty("display", "flex")
    DOMWidgetArtifact(el)
  }

  val updateDirection = updateStyleProperty[Direction](
    artifact.el,
    "flex-direction",
    (value, set, remove) =>
      value match {
        case Horizontal    => remove()
        case HorizontalRev => set("row-reverse")
        case Vertical      => set("column")
        case VerticalRev   => set("column-reverse")
    }
  ) _

  val updateJustify = updateStyleProperty[Justify](
    artifact.el,
    "justify-content",
    (value, set, remove) =>
      value match {
        case Start        => remove()
        case End          => set("flex-end")
        case Center       => set("center")
        case SpaceBetween => set("space-between")
        case SpaceAround  => set("space-around")
    }
  ) _

  updateDirection(context.direction)
  updateJustify(context.justify)

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
      updateDirection(direction)
    case SetJustify(justify) =>
      updateJustify(justify)
    case msg =>
      super.process(msg)
  }
}

class DOMLinearLayoutBuilder(widgetManager: WidgetManager) extends WidgetBuilder(LinearLayoutProtocol) {
  import LinearLayoutProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) = {
    new DOMLinearLayout(widgetId, context, widgetManager)
  }
}
