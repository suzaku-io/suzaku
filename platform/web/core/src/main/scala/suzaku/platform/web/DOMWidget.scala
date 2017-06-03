package suzaku.platform.web

import arteria.core.Protocol
import org.scalajs.dom
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.style.StyleBaseProperty
import suzaku.ui.{WidgetArtifact, WidgetWithProtocol}

case class DOMWidgetArtifact[E <: dom.Node](el: E) extends WidgetArtifact {}

abstract class DOMWidget[P <: Protocol, E <: dom.Node] extends WidgetWithProtocol[P] {
  override type Artifact = DOMWidgetArtifact[E]
  override type W        = DOMWidget[P, E]

  @inline protected def modifyDOM(f: E => Unit): Unit = f(artifact.el)

  def updateChildren(ops: Seq[ChildOp], mapWidget: Int => W): Unit = {
    val el    = artifact.el
    var child = el.firstChild
    ops.foreach {
      case NoOp(n) =>
        for (_ <- 0 until n) child = child.nextSibling
      case InsertOp(widgetId) =>
        val widget = mapWidget(widgetId)
        widget.setParent(this)
        el.insertBefore(widget.artifact.el, child)
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
        val widget = mapWidget(widgetId)
        widget.setParent(this)
        el.replaceChild(widget.artifact.el, child)
        child = next
    }
  }

  override def process = {
    case other =>
      super.process(other)
  }

  override def applyStyleClasses(styles: List[Int]): Unit = {
    val el = artifact.el.asInstanceOf[dom.html.Element]
    el.className = styles match {
      case Nil         => ""
      case head :: Nil => DOMWidget.mapStyleClass(head)
      case _           => styles.map(DOMWidget.mapStyleClass).mkString(" ")
    }
  }

  override def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit = {
    DOMWidget.extractStyle(prop) match {
      case ("", _) => // ignore
      case (name, value) =>
        updateStyleProperty(name, remove, value)
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
  def mapStyleClass(id: Int): String = {
    "_S" + Integer.toString(id, 36)
  }

  def extractStyle(prop: StyleBaseProperty): (String, String) = {
    import suzaku.ui.style._
    prop match {
      case EmptyStyle => ("", "")

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
