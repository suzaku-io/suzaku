package suzaku.platform.web

import boopickle.Default._
import org.scalajs.dom
import org.scalajs.dom.raw.DOMParser
import suzaku.platform.{Logger, Platform}
import suzaku.ui._
import suzaku.ui.resource.{Base64ImageResource, ResourceRegistration, SVGImageResource}
import suzaku.ui.style.{Active, Hover, NthChild, PseudoClass}

class DOMUIManager(logger: Logger, platform: Platform) extends UIManager(logger, platform) {
  val root = DOMWidgetArtifact(dom.document.getElementById("root").asInstanceOf[dom.html.Div])

  val eventHandler = new DOMEventHandler(dom.document)
  eventHandler.initialize()

  def addListener(
      eventType: EventType)(widgetId: Int, target: dom.Element, callback: DOMEvent[eventType.Event] => Unit): Unit = {
    eventHandler.addListener(eventType)(widgetId, target, callback)
  }

  def removeListener(eventType: EventType, widgetId: Int, target: dom.Element): Unit = {
    eventHandler.removeListener(eventType, widgetId, target)
  }

  def addCaptureListener(
      eventType: EventType)(widgetId: Int, target: dom.Element, callback: DOMEvent[eventType.Event] => Unit): Unit = {
    eventHandler.addCaptureListener(eventType)(widgetId, target, callback)
  }

  def removeCaptureListener(eventType: EventType, widgetId: Int, target: dom.Element): Unit = {
    eventHandler.removeCaptureListener(eventType, widgetId, target)
  }

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
      case null  => // nothing to reset
      case style =>
        // remove all definitions
        style.innerHTML = ""
    }
  }

  override protected def addEmbeddedResources(resources: Seq[ResourceRegistration]) = {
    // store embedded SVG images as symbols
    val symbols = resources.collect {
      case ResourceRegistration(id, SVGImageResource(svg, (x0, y0, x1, y1))) =>
        // create SVG symbol as text and parse it to DOM
        val xml = s"""<symbol xmlns="http://www.w3.org/2000/svg" id="suzaku-svg-$id" viewbox="$x0 $y0 $x1 $y1">$svg</symbol>"""
        println(xml)
        val parser = new DOMParser
        val symbol = parser.parseFromString(xml, "application/xml")
        symbol.documentElement.asInstanceOf[dom.svg.Symbol]
    }
    if (symbols.nonEmpty) {
      var svgRoot = dom.document.querySelector("svg#suzaku-svg").asInstanceOf[dom.svg.SVG]
      if (svgRoot == null) {
        svgRoot = dom.document.createElementNS("http://www.w3.org/2000/svg", "svg").asInstanceOf[dom.svg.SVG]
        svgRoot.id = "suzaku-svg"
        svgRoot.style.display = "none"
        dom.document.body.appendChild(svgRoot)
      }
      symbols.foreach(symbol => svgRoot.appendChild(symbol))
    }
    // store embedded Base64 bitmap images as CSS background-images
    val images = resources.collect {
      case ResourceRegistration(id, Base64ImageResource(data, width, height, mimeType)) =>
        // generate appropriate CSS
      s""".suzaku-image-$id {
         |  background-image: url(data:${mimeType.getOrElse("image")};base64,$data);
         |  background-repeat: no-repeat;
         |  background-size: contain;
         |}
       """.stripMargin
    }
    if(images.nonEmpty) {
      var style = dom.document.querySelector("style#suzaku-images").asInstanceOf[dom.html.Style]
      if (style == null) {
        style = dom.document.createElement("style").asInstanceOf[dom.html.Style]
        style.`type` = "text/css"
        style.id = "suzaku-images"
        dom.document.head.appendChild(style)
      }
      images.foreach(image => style.appendChild(dom.document.createTextNode(image)))
    }
  }
}
