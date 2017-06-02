package suzaku.platform.web

import arteria.core._
import boopickle.Default._
import org.scalajs.dom
import suzaku.platform.Logger
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.WidgetProtocol.UpdateStyle
import suzaku.ui._
import suzaku.ui.style.{StyleIds, StyleProperty}
import suzaku.ui.style.StyleRegistry.StyleRegistration

case class DOMWidgetArtifact[E <: dom.Node](el: E) extends WidgetArtifact {}

abstract class DOMWidget[P <: Protocol, E <: dom.Node] extends WidgetWithProtocol[P] {
  override type Artifact = DOMWidgetArtifact[E]
  override type V        = DOMWidget[P, E]

  @inline protected def modifyDOM(f: E => Unit): Unit = f(artifact.el)

  def updateChildren(ops: Seq[ChildOp], widget: Int => V): Unit = {
    val el    = artifact.el
    var child = el.firstChild
    ops.foreach {
      case NoOp(n) =>
        for (_ <- 0 until n) child = child.nextSibling
      case InsertOp(widgetId) =>
        el.insertBefore(widget(widgetId).artifact.el, child)
      case RemoveOp(n) =>
        for (_ <- 0 until n) {
          val next = child.nextSibling
          el.removeChild(child)
          child = next
        }
      case MoveOp(idx) =>
        el.insertBefore(el.childNodes.item(idx), child)
      case ReplaceOp(widgetId) =>
        val next = child.nextSibling
        el.replaceChild(widget(widgetId).artifact.el, child)
        child = next
    }
  }

  override def process = {
    case UpdateStyle(props) =>
      props.foreach {
        case (StyleIds(styles), remove) =>
          val el = artifact.el.asInstanceOf[dom.html.Element]
          el.className = if (remove) {
            ""
          } else {
            if (styles.size == 1) {
              DOMWidget.mapStyleClass(mapStyle(styles.head.id))
            } else {
              styles.map(s => DOMWidget.mapStyleClass(mapStyle(s.id))).mkString(" ")
            }
          }
        case (prop, remove) =>
          DOMWidget.extractStyle(prop) match {
            case ("", "") => // ignore
            case (name, value) =>
              updateStyleProperty(name, remove, value)
          }
      }
  }

  // helpers
  protected def textNode(text: String): dom.Text = dom.document.createTextNode(text)

  protected def updateStyleProperty[A](el: dom.html.Element, property: String, f: (A, String => Unit, () => Unit) => Unit)(
      value: A) = {
    f(value, el.style.setProperty(property, _), () => el.style.removeProperty(property))
  }

  protected def updateStyleProperty(property: String, remove: Boolean, value: String) =
    if (remove)
      artifact.el.asInstanceOf[dom.html.Element].style.removeProperty(property)
    else
      artifact.el.asInstanceOf[dom.html.Element].style.setProperty(property, value)
}

object DOMWidget {
  val hex = Array.tabulate(256)(c => f"$c%02x")

  def mapStyleClass(id: Int): String = {
    s"__s$id"
  }

  def extractStyle(prop: StyleProperty): (String, String) = {
    import suzaku.ui.style._
    prop match {
      case EmptyStyle => ("", "")

      case StyleIds(_) => ("", "")

      case Color(RGB(c)) =>
        ("color", s"rgb(${c.r},${c.g},${c.b})")
      case Color(RGBA(c, a)) =>
        ("color", s"rgba(${c.r},${c.g},${c.b},$a)")
      case BackgroundColor(RGB(c)) =>
        ("background-color", s"rgb(${c.r},${c.g},${c.b})")
      case BackgroundColor(RGBA(c, a)) =>
        ("background-color", s"rgba(${c.r},${c.g},${c.b},$a)")

      case Order(n) =>
        ("order", n.toString)
      case Width(l) =>
        ("width", l.toString)
      case Height(l) =>
        ("height", l.toString)
    }
  }
}

object DOMEmptyWidget extends DOMWidget[Protocol, dom.Comment] {
  override def artifact = DOMWidgetArtifact(dom.document.createComment("EMPTY"))
}

class DOMWidgetRenderer(logger: Logger) extends WidgetRenderer(logger) {
  val root                 = DOMWidgetArtifact(dom.document.getElementById("root"))
  override def emptyWidget = DOMEmptyWidget

  override def mountRoot(node: WidgetArtifact) = {
    import org.scalajs.dom.ext._

    val domElement = node.asInstanceOf[DOMWidgetArtifact[_ <: dom.Node]].el
    // remove all children
    root.el.childNodes.foreach(root.el.removeChild)
    // add new root element
    root.el.appendChild(domElement)
  }

  override def addStyles(styles: List[(Int, List[StyleProperty])]): Unit = {
    // create CSS block for all styles
    val styleDef = styles.map {
      case (id, styleProps) =>
        val className = DOMWidget.mapStyleClass(id)
        val css = styleProps.map { prop =>
          val (name, value) = DOMWidget.extractStyle(prop)
          s"$name:$value;"
        }

        s".$className { ${css.mkString("")} }"
    }.mkString("\n")

    val style = dom.document.createElement("style").asInstanceOf[dom.html.Style]
    style.`type` = "text/css"
    style.appendChild(dom.document.createTextNode(styleDef))
    dom.document.head.appendChild(style)
  }
}
