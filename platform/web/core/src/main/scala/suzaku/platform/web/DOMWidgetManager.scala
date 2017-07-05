package suzaku.platform.web

import boopickle.Default._
import org.scalajs.dom
import suzaku.platform.{Logger, Platform}
import suzaku.ui._
import suzaku.ui.style.{Active, Hover, NthChild, PseudoClass, StyleBaseProperty}

class DOMWidgetManager(logger: Logger, platform: Platform) extends WidgetManager(logger, platform) {
  val root = DOMWidgetArtifact(dom.document.getElementById("root").asInstanceOf[dom.html.Div])

  override def emptyWidget(widgetId: Int) = new DOMEmptyWidget(widgetId, this)

  override def mountRoot(node: WidgetArtifact) = {
    import org.scalajs.dom.ext._

    val domElement = node.asInstanceOf[DOMWidgetArtifact[_ <: dom.Node]].el
    // remove all children
    root.el.childNodes.foreach(root.el.removeChild)
    // add new root element
    root.el.appendChild(domElement)
  }

  override def addStyles(styles: Seq[RegisteredStyle]): Unit = {
    // create CSS block for given styles
    val styleDef = styles
      .map {
        case RegisteredStyle(styleId, styleName, styleProps, _, _, _) =>
          val className = DOMWidget.getClassName(styleId)
          val (regularStyles, pseudoStyles) = styleProps.foldLeft((List.empty[String], Map.empty[String, List[String]])) {
            case ((regular, pseudo), pc: PseudoClass) =>
              val ps = pc.props.map { prop =>
                val (name, value) = DOMWidget.extractStyle(prop)(this)
                if (name.nonEmpty) s"$name:$value;" else ""
              }
              val name = pc match {
                case _: Hover          => "hover"
                case _: Active         => "active"
                case NthChild(a, 0, _) => s"nth-child($a)"
                case NthChild(a, b, _) => s"nth-child(${a}n+$b)"
              }
              (regular, pseudo.updated(name, ps ::: pseudo.getOrElse(name, Nil)))
            case ((regular, pseudo), prop) =>
              val (name, value) = DOMWidget.extractStyle(prop)(this)
              if (name.nonEmpty) (s"$name:$value;" :: regular, pseudo) else (regular, pseudo)
          }

          val css = s".$className { ${regularStyles.mkString("")} }"

          val pseudoCss =
            pseudoStyles
              .map {
                case (name, innerStyles) =>
                  s"\n.$className:$name { ${innerStyles.mkString("")} }"
              }
              .mkString("")
          css + pseudoCss + s" /* $styleName */"
      }
      .mkString("\n", "\n", "\n")

    // update a single <style> node with the CSS definitions
    var style = dom.document.querySelector("style#suzaku-style").asInstanceOf[dom.html.Style]
    if (style == null) {
      style = dom.document.createElement("style").asInstanceOf[dom.html.Style]
      style.`type` = "text/css"
      style.id = "suzaku-style"
      dom.document.head.appendChild(style)
    }
    style.appendChild(dom.document.createTextNode(styleDef))
  }

  override def resetStyles(): Unit = {
    dom.document.querySelector("style#suzaku-style").asInstanceOf[dom.html.Style] match {
      case null => // nothing to reset
      case style =>
        // remove all definitions
        style.innerHTML = ""
    }
  }
}
