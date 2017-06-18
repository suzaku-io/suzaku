package suzaku.platform.web

import arteria.core.Protocol
import org.scalajs.dom
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.style.StyleBaseProperty
import suzaku.ui.{WidgetArtifact, WidgetManager, WidgetWithProtocol}

case class DOMWidgetArtifact[E <: dom.html.Element](el: E) extends WidgetArtifact {}

abstract class DOMWidget[P <: Protocol, E <: dom.html.Element](widgetId: Int, widgetManager: WidgetManager)
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
    styles match {
      case head :: Nil => artifact.el.className = DOMWidget.getClassName(head)
      case Nil         => artifact.el.removeAttribute("class")
      case _           => artifact.el.className = styles.map(DOMWidget.getClassName).mkString(" ")
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
      artifact.el.style.removeProperty(property)
    else
      artifact.el.style.setProperty(property, value)
}

object DOMWidget {
  import suzaku.ui.style._

  def getClassName(id: Int): String = {
    "_S" + Integer.toString(id, 10)
  }

  private def show(c: RGBColor): String = c match {
    case RGB(_)     => s"rgb(${c.r},${c.g},${c.b})"
    case RGBA(_, a) => s"rgba(${c.r},${c.g},${c.b},$a)"
  }

  private def show(l: LineStyle): String = l match {
    case LineNone   => "none"
    case LineHidden => "hidden"
    case LineSolid  => "solid"
    case LineDotted => "dotted"
    case LineDashed => "dashed"
    case LineInset  => "inset"
    case LineOutset => "outset"
    case LineDouble => "double"
  }

  private def show(w: WidthDimension): String = w match {
    case WidthThin      => "thin"
    case WidthMedium    => "medium"
    case WidthThick     => "thick"
    case WidthLength(l) => l.toString
  }

  private def show(size: FontDimension): String = size match {
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

  private def show(weight: WeightDimension): String = weight match {
    case WeightNormal       => "normal"
    case WeightBold         => "bold"
    case WeightBolder       => "bolder"
    case WeightLighter      => "lighter"
    case WeightValue(value) => (math.round(value / 100.0) * 100 max 100 min 900).toString
  }

  private def show(l: LengthDimension): String = l match {
    case LengthU(value)   => value.toString
    case LengthPx(value)  => s"${value}px"
    case LengthPct(value) => s"$value%"
    case LengthEm(value)  => s"${value}em"
    case LengthRem(value) => s"${value}rem"
    case LengthVw(value)  => s"${value}vw"
    case LengthVh(value)  => s"${value}vh"
    case LengthAuto       => "auto"
  }

  def expand[A <: Direction](prop: A)(show: A => String, name: String, ext: String = ""): (String, String) = {
    val extStr = if (ext.isEmpty) "" else "-" + ext
    prop match {
      case _: DirectionTop    => (s"$name-top$extStr", show(prop))
      case _: DirectionRight  => (s"$name-right$extStr", show(prop))
      case _: DirectionBottom => (s"$name-bottom$extStr", show(prop))
      case _: DirectionLeft   => (s"$name-left$extStr", show(prop))
    }
  }

  def extractStyle(prop: StyleBaseProperty): (String, String) = {
    prop match {
      case Color(c)           => ("color", show(c))
      case BackgroundColor(c) => ("background-color", show(c))

      case FontFamily(family) => ("font-family", family.mkString("\"", "\",\"", "\""))
      case FontSize(size)     => ("font-size", show(size))
      case FontWeight(weight) => ("font-weight", show(weight))
      case FontItalics        => ("font-style", "italics")

      case Width(l)         => ("width", show(l))
      case Height(l)        => ("height", show(l))
      case MaxWidth(value)  => ("max-width", show(value))
      case MaxHeight(value) => ("max-height", show(value))
      case MinWidth(value)  => ("min-width", show(value))
      case MinHeight(value) => ("min-height", show(value))

      case p: Margin  => expand(p)(l => show(l.value), "margin")
      case p: Padding => expand(p)(l => show(l.value), "padding")
      case p: Offset  => expand(p)(l => show(l.value), "offset")

      case p: BorderWidth => expand(p)(w => show(w.value), "border", "width")
      case p: BorderStyle => expand(p)(s => show(s.style), "border", "style")
      case p: BorderColor => expand(p)(c => show(c.color), "border", "color")

      case OutlineWidth(w) => ("outline-width", show(w))
      case OutlineStyle(s) => ("outline-style", show(s))
      case OutlineColor(c) => ("outline-color", show(c))

      case EmptyStyle | RemapClasses(_) => ("", "")
    }
  }
}

class DOMEmptyWidget(widgetId: Int, widgetManager: WidgetManager)
    extends DOMWidget[Protocol, dom.html.Element](widgetId, widgetManager) {

  // a Comment is not really an HTMLElement, but we will mark it as such to make it compile :)
  override val artifact = DOMWidgetArtifact(dom.document.createComment("EMPTY").asInstanceOf[dom.html.Element])

  override def applyStyleClasses(styles: List[Int]): Unit = {}

  override def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit = {}
}
