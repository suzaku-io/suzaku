package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.{DOMUIManager, DOMWidget, DOMWidgetArtifact}
import suzaku.ui.layout._
import suzaku.ui.{Widget, WidgetBuilder}

class DOMLinearLayout(widgetId: Int, context: LinearLayoutProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidget[LinearLayoutProtocol.type, dom.html.Div](widgetId, widgetManager) with DOMLayout {
  import Direction._
  import Justify._
  import LinearLayoutProtocol._

  val artifact = {
    val el = tag[dom.html.Div]("div")
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
        val widget = c.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
        el.appendChild(widget.artifact.el)
        resolveLayout(widget, widget.layoutProperties)
      }
    }
  }

  override protected val layoutPropNames = List(
    "order",
    "align-self",
    "flex-grow"
  )

  override protected def resolveLayout(modWidget : (dom.html.Element => Unit) => Unit, layoutProperty: LayoutProperty): Unit = {
    layoutProperty match {
      case Order(n) =>
        modWidget { el =>
          if (n != 0)
            el.style.setProperty("order", n.toString)
        }
      case AlignSelf(alignment) =>
        modWidget { el =>
          if (alignment != AlignAuto) {
            el.style.setProperty(
              "align-self",
              alignment match {
                case AlignStart => "flex-start"
                case AlignEnd => "flex-end"
                case AlignCenter => "center"
                case AlignBaseline => "baseline"
                case AlignStretch => "stretch"
                case AlignAuto => "auto"
              }
            )
          }
        }
      case LayoutWeight(w) =>
        modWidget { el =>
          if (w != 0) {
            el.style.setProperty("flex-grow", w.toString)
          }
        }
      case LayoutSlotId(layoutId) =>
        // allow but ignore

      case other =>
        throw new IllegalArgumentException(s"Layout property $other is not supported for LinearLayout")
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

class DOMLinearLayoutBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(LinearLayoutProtocol) {
  import LinearLayoutProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) = {
    new DOMLinearLayout(widgetId, context, widgetManager)
  }
}
