package suzaku.platform.web

import boopickle.Default._
import org.scalajs.dom
import suzaku.platform.{Logger, Platform}
import suzaku.ui._
import suzaku.ui.style.{Active, Hover, PseudoClass, StyleBaseProperty}

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

  override def addStyles(styles: List[(Int, String, List[StyleBaseProperty])]): Unit = {
    // create CSS block for all styles
    val styleDef = styles
      .map {
        case (styleId, styleName, styleProps) =>
          val className = DOMWidget.getClassName(styleId)
          val (regularStyles, pseudoStyles) = styleProps.foldLeft((List.empty[String], Map.empty[String, List[String]])) {
            case ((regular, pseudo), pc: PseudoClass) =>
              val ps = pc.props.map { prop =>
                val (name, value) = DOMWidget.extractStyle(prop)
                s"$name:$value;"
              }
              val name = pc match {
                case _: Hover  => "hover"
                case _: Active => "active"
              }

              (regular, pseudo.updated(name, ps ::: pseudo.getOrElse(name, Nil)))
            case ((regular, pseudo), prop) =>
              val (name, value) = DOMWidget.extractStyle(prop)
              (s"$name:$value;" :: regular, pseudo)
          }

          val css = s".$className { ${regularStyles.mkString("")} }"

          val pseudoCss =
            pseudoStyles.map { case (name, innerStyles) =>
              s"\n.$className:$name { ${innerStyles.mkString("")} }"
            }.mkString("")
          css + pseudoCss + s" /* $styleName */"
      }
      .mkString("\n", "\n", "\n")

    val style = dom.document.createElement("style").asInstanceOf[dom.html.Style]
    style.`type` = "text/css"
    style.appendChild(dom.document.createTextNode(styleDef))
    dom.document.head.appendChild(style)
  }
}
