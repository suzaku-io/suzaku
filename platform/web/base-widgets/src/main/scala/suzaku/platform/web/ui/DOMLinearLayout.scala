package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.layout._
import suzaku.ui.{Widget, WidgetBuilder, WidgetManager}

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

  override def setChildren(children: Seq[Widget]) = {
    import org.scalajs.dom.ext._
    modifyDOM { el =>
      el.childNodes.foreach(el.removeChild)
      children.foreach { c =>
        val widget = c.asInstanceOf[DOMWidget[_, _ <: dom.Node]]
        el.appendChild(widget.artifact.el)
        resolveLayout(widget, widget.layoutProperties)
      }
    }
  }

  override def resolveLayout(w: Widget, layoutProperties: List[LayoutProperty]): Unit = {
    val widget    = w.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
    val modWidget = (f: dom.html.Element => Unit) => widget.modifyDOM(f)

    layoutProperties foreach {
      case AlignSelf(alignment) =>
        modWidget { el =>
          el.style.removeProperty("align-self")
          if (alignment != AlignAuto) {
            el.style.setProperty(
              "align-self",
              alignment match {
                case AlignStart    => "start"
                case AlignEnd      => "end"
                case AlignCenter   => "center"
                case AlignBaseline => "baseline"
                case AlignStretch  => "stretch"
                case AlignAuto     => "auto"
              }
            )
          }
        }
      case LayoutWeight(weight) =>
        modWidget { el =>
          el.style.removeProperty("flex-grow")
          if (weight != 0) {
            el.style.setProperty("flex-grow", weight.toString)
          }
        }
      case _ => // ignore others
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
