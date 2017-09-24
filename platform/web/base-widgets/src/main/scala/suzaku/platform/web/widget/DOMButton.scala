package suzaku.platform.web.widget

import org.scalajs.dom
import suzaku.platform.web._
import suzaku.ui.WidgetBuilder
import suzaku.ui.style.Palette
import suzaku.widget.ButtonProtocol

class DOMButton(widgetId: Int, context: ButtonProtocol.ChannelContext)(implicit uiManager: DOMUIManager)
    extends DOMWidget[ButtonProtocol.type, dom.html.Button](widgetId, uiManager) {
  import ButtonProtocol._

  val labelNode = textNode(context.label)
  val iconNode  = context.icon.map(image => imageNode(image, Some("1em"), Some("1em"), Some("currentColor")))

  val artifact = {
    val el = tag[dom.html.Button]("button")
    uiManager.addListener(ClickEvent)(widgetId, el, onClick)

    el.appendChild(labelNode)
    iconNode.foreach(el.appendChild)

    DOMWidgetArtifact(el)
  }

  override protected def baseStyleClasses = List(DOMButton.style.base)

  def onClick(e: DOMEvent[dom.MouseEvent]): Unit = {
    channel.send(Click)
  }

  override def process = {
    case SetLabel(label) =>
      modifyDOM(_ => labelNode.data = label)
    case SetIcon(icon) =>
    case msg =>
      super.process(msg)
  }

  override def closed(): Unit = {
    uiManager.removeListener(ClickEvent, widgetId, artifact.el)
  }
}

class DOMButtonBuilder(uiManager: DOMUIManager) extends WidgetBuilder(ButtonProtocol) {
  import ButtonProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMButton(widgetId, context)(uiManager)
}

object DOMButton extends WidgetStyleProvider {
  class Style(uiManager: DOMUIManager) extends WidgetStyle()(uiManager) {
    import DOMWidget._
    val base = uiManager.registerCSSClass(palette => s"""text-align: center;
         |text-decoration: none;
         |user-select: none;
         |vertical-align: middle;
         |white-space: nowrap;
         |padding: 0 .75em;
         |margin: 0 .25em 0 0;
         |border: none;
         |cursor: pointer;
         |display: inline-block;
         |height: ${show(styleConfig.buttonHeight)};
         |line-height: ${show(styleConfig.buttonHeight)};
         |border-radius: ${show(styleConfig.buttonBorderRadius)};
         |box-shadow: ${styleConfig.buttonBoxShadow};
         |color: ${show(palette(Palette.Primary).color.foregroundColor)};
         |background-color: ${show(palette(Palette.Primary).color.backgroundColor)};
         |font-family: ${styleConfig.buttonFontFamily};
         |font-size: ${show(styleConfig.buttonFontSize)};
         |font-weight: ${show(styleConfig.buttonFontWeight)};
        """.stripMargin)
    uiManager.addCSS(s".$base:hover", s"""box-shadow: ${styleConfig.buttonBoxShadowHover};""".stripMargin)
    uiManager.addCSS(s".$base:active", s"""box-shadow: ${styleConfig.buttonBoxShadowActive};""".stripMargin)
  }

  override def buildStyle(implicit uiManager: DOMUIManager) = new Style(uiManager)
}
