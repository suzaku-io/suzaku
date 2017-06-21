package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact}
import suzaku.ui.layout._
import suzaku.ui.{Widget, WidgetBuilder, WidgetManager}

class DOMGridLayout(widgetId: Int, context: GridLayoutProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidget[GridLayoutProtocol.type, dom.html.Div](widgetId, widgetManager)
    with DOMLayout {
  import GridLayoutProtocol._

  val artifact = {
    val el = tag[dom.html.Div]("div")
    el.style.setProperty("display", "grid")
    DOMWidgetArtifact(el)
  }

  def show(track: TrackDef): String = track match {
    case TrackSize(size)       => DOMWidget.show(size)
    case TrackMinMax(min, max) => s"minmax(${DOMWidget.show(min)},${DOMWidget.show(max)})"
  }

  val updateCols = updateStyleProperty[TrackTemplate](
    artifact.el,
    "grid-template-columns",
    (value, set, remove) => {
      if (value.tracks.isEmpty)
        remove()
      else
        set(value.tracks.map(show).mkString(" "))
    }
  ) _

  val updateRows = updateStyleProperty[TrackTemplate](
    artifact.el,
    "grid-template-rows",
    (value, set, remove) => {
      if (value.tracks.isEmpty)
        remove()
      else
        set(value.tracks.map(show).mkString(" "))
    }
  ) _

  val updateSlots = updateStyleProperty[Seq[Seq[LayoutId]]](
    artifact.el,
    "grid-template-areas",
    (slots, set, remove) => {
      if (slots.isEmpty)
        remove()
      else
        set(slots.map(row => row.map(lid => s"_L${lid.id}").mkString(" ")).mkString("'", "' '", "'"))
    }
  ) _

  updateCols(context.grid.cols)
  updateRows(context.grid.rows)
  updateSlots(context.grid.slots)

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

  override protected val layoutPropNames = List(
    "grid-area",
    "align-self",
    "justify-self"
  )

  override protected def resolveLayout(modWidget: (dom.html.Element => Unit) => Unit,
                                       layoutProperty: LayoutProperty): Unit = {
    layoutProperty match {
      case LayoutSlotId(layoutGroup) =>
        println(s"Set grid area to ${layoutGroup.id}")
        modWidget { el =>
          el.style.setProperty("grid-area", s"_L${layoutGroup.id}")
        }

      case AlignSelf(alignment) =>
        modWidget { el =>
          el.style.setProperty(
            "align-self",
            alignment match {
              case AlignStart   => "start"
              case AlignEnd     => "end"
              case AlignCenter  => "center"
              case AlignStretch => "stretch"
              case _ =>
                throw new IllegalArgumentException(s"Alignment $alignment is not supported for Grid layout")
            }
          )
        }

      case JustifySelf(alignment) =>
        modWidget { el =>
          el.style.setProperty(
            "justify-self",
            alignment match {
              case AlignStart   => "start"
              case AlignEnd     => "end"
              case AlignCenter  => "center"
              case AlignStretch => "stretch"
              case _ =>
                throw new IllegalArgumentException(s"Alignment $alignment is not supported for Grid layout")
            }
          )
        }
    }
  }
}

class DOMGridLayoutBuilder(widgetManager: WidgetManager) extends WidgetBuilder(GridLayoutProtocol) {
  import GridLayoutProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) = {
    new DOMGridLayout(widgetId, context, widgetManager)
  }
}
