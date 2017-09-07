package suzaku.platform.web.ui

import org.scalajs.dom
import suzaku.platform.web.DOMWidget
import suzaku.ui.layout._
import suzaku.ui.{Widget, WidgetParent}

trait DOMLayout extends WidgetParent { self: DOMWidget[_, _] =>
  private val commonPropNames = List(
    "z-index"
  )

  protected val layoutPropNames: List[String]

  protected def resolveLayout(modWidget : (dom.html.Element => Unit) => Unit, layoutProperty: LayoutProperty): Unit

  override def resolveLayout(w: Widget, layoutProperties: List[LayoutProperty]): Unit = {
    val widget    = w.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
    val modWidget = (f: dom.html.Element => Unit) => widget.modifyDOM(f)

    // println(s"Resolving layout: $layoutProperties")
    // only for real HTML elements
    if (!scalajs.js.isUndefined(widget.artifact.el.style)) {
      // first remove all layout properties
      modWidget { el =>
        commonPropNames.foreach(el.style.removeProperty)
        layoutPropNames.foreach(el.style.removeProperty)
      }
      layoutProperties foreach {
        case ZOrder(n) =>
          modWidget { el =>
            el.style.setProperty("z-index", n.toString)
          }

        case other =>
          resolveLayout(modWidget, other)
      }
    }
  }
}
