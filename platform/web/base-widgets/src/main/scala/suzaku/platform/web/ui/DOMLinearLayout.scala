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

  val updateAlignment = updateStyleProperty[Alignment](
    artifact.el,
    "align-items",
    (value, set, remove) =>
      value match {
        case AlignAuto     => remove()
        case AlignStart    => set("flex-start")
        case AlignEnd      => set("flex-end")
        case AlignCenter   => set("center")
        case AlignBaseline => set("baseline")
        case AlignStretch  => remove()
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

  private val allPropNames = List(
    "align-self",
    "flex-grow",
    "order",
    "z-index"
  )

  override def resolveLayout(w: Widget, layoutProperties: List[LayoutProperty]): Unit = {
    val widget    = w.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
    val modWidget = (f: dom.html.Element => Unit) => widget.modifyDOM(f)

    // only for real HTML elements
    if (!scalajs.js.isUndefined(widget.artifact.el.style)) {
      // first remove all layout properties
      modWidget { el =>
        allPropNames.foreach(el.style.removeProperty)
      }
      layoutProperties foreach {
        case AlignSelf(alignment) =>
          modWidget { el =>
            if (alignment != AlignAuto) {
              el.style.setProperty(
                "align-self",
                alignment match {
                  case AlignStart    => "flex-start"
                  case AlignEnd      => "flex-end"
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
            if (weight != 0) {
              el.style.setProperty("flex-grow", weight.toString)
            }
          }
        case Order(n) =>
          modWidget { el =>
            if (n != 0)
              el.style.setProperty("order", n.toString)
          }
        case ZOrder(n) =>
          modWidget { el =>
            el.style.setProperty("z-index", n.toString)
          }

        case _ => // ignore others
      }
    }
  }

  override def process = {
    case SetDirection(direction) =>
      updateDirection(direction)
    case SetJustify(justify) =>
      updateJustify(justify)
    case SetAlignment(align) =>
      updateAlignment(align)
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
