package suzaku.platform.web

import arteria.core._
import boopickle.Default._
import org.scalajs.dom
import suzaku.platform.Logger
import suzaku.ui.LayoutParameter.{NoLayout, Order}
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.WidgetProtocol.UpdateLayout
import suzaku.ui.{WidgetArtifact, WidgetRenderer, WidgetWithProtocol}

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
    case UpdateLayout(params) =>
      params.foreach {
        case (Order(n), remove) =>
          updateStyle("order", remove, n.toString)
        case (NoLayout, _) =>
          // no-op
      }
  }

  // helpers
  protected def textNode(text: String): dom.Text = dom.document.createTextNode(text)

  protected def updateStyle[A](el: dom.html.Element, property: String, f: (A, String => Unit, () => Unit) => Unit)(
      value: A) = {
    f(value, el.style.setProperty(property, _), () => el.style.removeProperty(property))
  }

  protected def updateStyle(property: String, remove: Boolean, value: String) =
    if (remove)
      artifact.el.asInstanceOf[dom.html.Element].style.removeProperty(property)
    else
      artifact.el.asInstanceOf[dom.html.Element].style.setProperty(property, value)
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
}
