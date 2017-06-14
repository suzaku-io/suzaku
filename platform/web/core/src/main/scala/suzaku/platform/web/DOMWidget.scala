package suzaku.platform.web

import arteria.core.Protocol
import org.scalajs.dom
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.style.StyleBaseProperty
import suzaku.ui.{WidgetArtifact, WidgetManager, WidgetWithProtocol}

case class DOMWidgetArtifact[E <: dom.Node](el: E) extends WidgetArtifact {}

abstract class DOMWidget[P <: Protocol, E <: dom.Node](widgetId: Int, widgetManager: WidgetManager)
    extends WidgetWithProtocol[P](widgetId, widgetManager) {
  override type Artifact = DOMWidgetArtifact[E]
  override type W        = DOMWidget[P, E]

  @inline protected[web] def modifyDOM(f: E => Unit): Unit = f(artifact.el)

  def updateChildren(ops: Seq[ChildOp], mapWidget: Int => W): Unit = {
    val el    = artifact.el
    var child = el.firstChild
    ops.foreach {
      case NoOp(n) =>
        for (_ <- 0 until n) child = child.nextSibling
      case InsertOp(id) =>
        val widget = mapWidget(id)
        el.insertBefore(widget.artifact.el, child)
      case RemoveOp(n) =>
        for (_ <- 0 until n) {
          val next = child.nextSibling
          el.removeChild(child)
          child = next
        }
      case MoveOp(idx) =>
        el.insertBefore(el.childNodes.item(idx), child)
      case ReplaceOp(id) =>
        val next   = child.nextSibling
        val widget = mapWidget(id)
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
      case head :: Nil => DOMWidget.getClassName(head)
      case _           => styles.map(DOMWidget.getClassName).mkString(" ")
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
  import suzaku.ui.style._
  def getClassName(id: Int): String = {
    "_S" + Integer.toString(id, 10)
  }

  def color2str(c: RGBColor): String = c match {
    case RGB(_)     => s"rgb(${c.r},${c.g},${c.b})"
    case RGBA(_, a) => s"rgba(${c.r},${c.g},${c.b},$a)"
  }

  def line2str(l: LineStyle): String = l match {
    case LineNone   => "none"
    case LineHidden => "hidden"
    case LineSolid  => "solid"
    case LineDotted => "dotted"
    case LineDashed => "dashed"
    case LineInset  => "inset"
    case LineOutset => "outset"
    case LineDouble => "double"
  }

  def width2str(w: WidthDimension): String = w match {
    case WidthThin      => "thin"
    case WidthMedium    => "medium"
    case WidthThick     => "thick"
    case WidthLength(l) => l.toString
  }

  def size2str(size: FontDimension): String = size match {
    case FontXXSmall   => "xx-small"
    case FontXSmall    => "x-small"
    case FontSmall     => "small"
    case FontSmaller   => "smaller"
    case FontMedium    => "medium"
    case FontLarge     => "large"
    case FontLarger    => "larger"
    case FontXLarge    => "x-large"
    case FontXXLarge   => "xx-large"
    case FontLength(s) => s.toString
  }

  def weight2str(weight: WeightDimension): String = weight match {
    case WeightNormal       => "normal"
    case WeightBold         => "bold"
    case WeightBolder       => "bolder"
    case WeightLighter      => "lighter"
    case WeightValue(value) => (math.round(value / 100.0) * 100 max 100 min 900).toString

  }

  def expand[A <: Direction](prop: A)(toStr: A => String, name: String, ext: String = ""): (String, String) = {
    val extStr = if (ext.isEmpty) "" else "-" + ext
    prop match {
      case _: DirectionTop    => (s"$name-top$extStr", toStr(prop))
      case _: DirectionRight  => (s"$name-right$extStr", toStr(prop))
      case _: DirectionBottom => (s"$name-bottom$extStr", toStr(prop))
      case _: DirectionLeft   => (s"$name-left$extStr", toStr(prop))
    }
  }

  def extractStyle(prop: StyleBaseProperty): (String, String) = {
    prop match {
      case EmptyStyle | RemapClasses(_) => ("", "")

      case Color(c)           => ("color", color2str(c))
      case BackgroundColor(c) => ("background-color", color2str(c))

      case FontFamily(family) => ("font-family", family.mkString("\"", "\",\"", "\""))
      case FontSize(size)     => ("font-size", size2str(size))
      case FontWeight(weight) => ("font-weight", weight2str(weight))
      case FontItalics        => ("font-style", "italics")

      case Order(n)         => ("order", n.toString)
      case ZOrder(n)        => ("z-index", n.toString)
      case Width(l)         => ("width", l.toString)
      case Height(l)        => ("height", l.toString)
      case MaxWidth(value)  => ("max-width", value.toString)
      case MaxHeight(value) => ("max-height", value.toString)
      case MinWidth(value)  => ("min-width", value.toString)
      case MinHeight(value) => ("min-height", value.toString)

      case p: Margin  => expand(p)(_.value.toString, "margin")
      case p: Padding => expand(p)(_.value.toString, "padding")
      case p: Offset  => expand(p)(_.value.toString, "offset")

      case p: BorderWidth => expand(p)(w => width2str(w.value), "border", "width")
      case p: BorderStyle => expand(p)(s => line2str(s.style), "border", "style")
      case p: BorderColor => expand(p)(c => color2str(c.color), "border", "color")

      case OutlineWidth(w) => ("outline-width", width2str(w))
      case OutlineStyle(s) => ("outline-style", line2str(s))
      case OutlineColor(c) => ("outline-color", color2str(c))
    }
  }
}

class DOMEmptyWidget(widgetId: Int, widgetManager: WidgetManager)
    extends DOMWidget[Protocol, dom.Comment](widgetId, widgetManager) {
  override def artifact = DOMWidgetArtifact(dom.document.createComment("EMPTY"))
}
