package suzaku.platform.web

import arteria.core.Protocol
import org.scalajs.dom
import suzaku.ui.UIProtocol.{ChildOp, InsertOp, MoveOp, NoOp, RemoveOp, ReplaceOp}
import suzaku.ui.resource._
import suzaku.ui.style.StyleBaseProperty
import suzaku.ui.{Widget, WidgetArtifact, WidgetWithProtocol}

case class DOMWidgetArtifact[E <: dom.html.Element](el: E) extends WidgetArtifact {}

abstract class DOMWidget[P <: Protocol, E <: dom.html.Element](widgetId: Int, widgetManager: DOMUIManager)
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
    DOMWidget.extractStyle(prop)(widgetManager) match {
      case ("", _) => // ignore
      case (name, value) =>
        updateStyleProperty(name, remove, value)
    }
  }

  // helpers

  protected def tag[T <: dom.html.Element](name: String): T = dom.document.createElement(name).asInstanceOf[T]

  protected def textNode(text: String): dom.Text = dom.document.createTextNode(text)

  protected def imageNode(res: ImageResource, width: Option[String], height: Option[String], fill: Option[String]): dom.Element = {
    res match {
      case embedded: EmbeddedResource =>
        widgetManager.getResource(embedded.resourceId) match {
          case Some(SVGImageResource(_, (x0,y0,x1,y1))) =>
            val svg = dom.document.createElementNS("http://www.w3.org/2000/svg", "svg").asInstanceOf[dom.svg.SVG]
            svg.setAttribute("viewBox", s"$x0 $y0 $x1 $y1")
            val use = dom.document.createElementNS("http://www.w3.org/2000/svg", "use").asInstanceOf[dom.svg.Use]
            use.setAttributeNS("http://www.w3.org/1999/xlink","href",s"#suzaku-svg-${embedded.resourceId}")
            svg.appendChild(use)
            width.foreach(svg.style.width = _)
            height.foreach(svg.style.height = _)
            fill.foreach(svg.style.fill = _)
            svg
          case Some(Base64ImageResource(_, imgWidth, imgHeight, _)) =>
            val img = dom.document.createElement("span").asInstanceOf[dom.html.Span]
            img.classList.add(s"suzaku-image-${embedded.resourceId}")
            img.style.display = "inline-block"
            img.style.width = width.getOrElse(s"${imgWidth}px")
            img.style.height = height.getOrElse(s"${imgHeight}px")
            img
          case Some(_) =>
            val img = dom.document.createElement("img").asInstanceOf[dom.html.Image]
            img.alt = "unknown type"
            img
          case None =>
            val img = dom.document.createElement("img").asInstanceOf[dom.html.Image]
            img.alt = "not found"
            img
        }
      case URIImageResource(uri, _) =>
        val img = dom.document.createElement("img").asInstanceOf[dom.html.Image]
        width.foreach(img.style.width = _)
        height.foreach(img.style.height = _)
        img.src = uri
        img
    }
  }

  protected def updateStyleProperty[A](el: dom.html.Element, property: String, f: (A, String => Unit, () => Unit) => Unit)(
      value: A): Unit = {
    f(value, el.style.setProperty(property, _), () => el.style.removeProperty(property))
  }

  protected def updateStyleProperty(property: String, remove: Boolean, value: String): Unit =
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

  def show(c: AbsoluteColor): String = c match {
    case rgb: RGB                 => s"rgb(${rgb.r},${rgb.g},${rgb.b})"
    case rgba: RGBAlpha           => s"rgba(${rgba.r},${rgba.g},${rgba.b},${rgba.alpha})"
    case HSL(h, s, l, alpha)      => s"hsl(${(h * 360).toInt},${(s * 100).toInt}%,${(l * 100).toInt}%,$alpha)"
    case _: AbsoluteColor =>
      val rgba = c.toRGBA
      s"rgba(${rgba.r},${rgba.g},${rgba.b},${rgba.alpha})"
  }

  def show(c: Color)(implicit colorProvider: ColorProvider): String = c match {
    case ac: AbsoluteColor =>
      show(ac)
    case PaletteRef(idx, variant) =>
      val entry = colorProvider.getColor(idx)
      show(entry.variant(variant))
  }

  def show(l: LineStyle): String = l match {
    case LineNone   => "none"
    case LineHidden => "hidden"
    case LineSolid  => "solid"
    case LineDotted => "dotted"
    case LineDashed => "dashed"
    case LineInset  => "inset"
    case LineOutset => "outset"
    case LineDouble => "double"
  }

  def show(w: WidthDimension): String = w match {
    case WidthThin      => "thin"
    case WidthMedium    => "medium"
    case WidthThick     => "thick"
    case WidthLength(l) => show(l)
  }

  def show(size: FontDimension): String = size match {
    case FontXXSmall   => "xx-small"
    case FontXSmall    => "x-small"
    case FontSmall     => "small"
    case FontSmaller   => "smaller"
    case FontMedium    => "medium"
    case FontLarge     => "large"
    case FontLarger    => "larger"
    case FontXLarge    => "x-large"
    case FontXXLarge   => "xx-large"
    case FontLength(s) => show(s)
  }

  def show(weight: WeightDimension): String = weight match {
    case WeightNormal       => "normal"
    case WeightBold         => "bold"
    case WeightBolder       => "bolder"
    case WeightLighter      => "lighter"
    case WeightValue(value) => (math.round(value / 100.0) * 100 max 100 min 900).toString
  }

  def show(l: LengthDimension): String = l match {
    case LengthU(value)   => value.toString
    case LengthPx(value)  => s"${value}px"
    case LengthPct(value) => s"$value%"
    case LengthEm(value)  => s"${value}em"
    case LengthRem(value) => s"${value}rem"
    case LengthVw(value)  => s"${value}vw"
    case LengthVh(value)  => s"${value}vh"
    case LengthFr(value)  => s"${value}fr"
    case LengthAuto       => "auto"
  }

  def show(layout: TableLayoutStyle): String = layout match {
    case TableLayoutAuto  => "auto"
    case TableLayoutFixed => "fixed"
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

  def extractStyle(prop: StyleBaseProperty)(implicit colorProvider: ColorProvider): (String, String) = {
    prop match {
      case ForegroundColor(c) => ("color", show(c))
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

      case TableLayout(layout) => ("table-layout", show(layout))

      case EmptyStyle | RemapClasses(_) | WidgetStyles(_) => ("", "")
    }
  }
}

abstract class DOMWidgetWithChildren[P <: Protocol, E <: dom.html.Element](widgetId: Int, widgetManager: DOMUIManager)
    extends DOMWidget[P, E](widgetId, widgetManager) {
  override def setChildren(children: Seq[Widget]) = {
    modifyDOM { el =>
      val prevChildren = el.childNodes
      var l            = prevChildren.length - 1
      while (l >= 0) {
        el.removeChild(prevChildren(l))
        l -= 1
      }
      children.foreach { c =>
        val widget = c.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
        el.appendChild(wrapChild(widget.artifact.el))
        resolveLayout(widget, widget.layoutProperties)
      }
    }
  }

  protected def wrapChild(el: dom.html.Element): dom.html.Element = el
}

class DOMEmptyWidget(widgetId: Int, widgetManager: DOMUIManager)
    extends DOMWidget[Protocol, dom.html.Element](widgetId, widgetManager) {

  // a Comment is not really an HTMLElement, but we will mark it as such to make it compile :)
  override val artifact = DOMWidgetArtifact(dom.document.createComment("EMPTY").asInstanceOf[dom.html.Element])

  override def applyStyleClasses(styles: List[Int]): Unit = {}

  override def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit = {}
}
