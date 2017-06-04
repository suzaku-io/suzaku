package suzaku.platform.web

import boopickle.Default._
import org.scalajs.dom
import suzaku.platform.{Logger, Platform}
import suzaku.ui._
import suzaku.ui.style.StyleBaseProperty

class DOMWidgetManager(logger: Logger, platform: Platform) extends WidgetManager(logger, platform) {
  val root                                = DOMWidgetArtifact(dom.document.getElementById("root"))
  override def emptyWidget(widgetId: Int) = new DOMEmptyWidget(widgetId, this)

  override def mountRoot(node: WidgetArtifact) = {
    import org.scalajs.dom.ext._

    val domElement = node.asInstanceOf[DOMWidgetArtifact[_ <: dom.Node]].el
    // remove all children
    root.el.childNodes.foreach(root.el.removeChild)
    // add new root element
    root.el.appendChild(domElement)
  }

  override def addStyles(styles: List[(Int, List[StyleBaseProperty])]): Unit = {
    // create CSS block for all styles
    val styleDef = styles
      .map {
        case (id, styleProps) =>
          val className = DOMWidget.getClassName(id)
          val css = styleProps.map { prop =>
            val (name, value) = DOMWidget.extractStyle(prop)
            s"$name:$value;"
          }

          s".$className { ${css.mkString("")} }"
      }
      .mkString("\n", "\n", "\n")

    val style = dom.document.createElement("style").asInstanceOf[dom.html.Style]
    style.`type` = "text/css"
    style.appendChild(dom.document.createTextNode(styleDef))
    dom.document.head.appendChild(style)
  }
}
