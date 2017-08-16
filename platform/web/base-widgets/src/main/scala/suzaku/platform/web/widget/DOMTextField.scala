package suzaku.platform.web.widget

import org.scalajs.dom
import suzaku.platform.web.{DOMUIManager, DOMWidget, DOMWidgetArtifact, StyleConfig}
import suzaku.ui.WidgetBuilder
import suzaku.ui.style.Palette
import suzaku.widget.TextFieldProtocol

class DOMTextField(widgetId: Int, context: TextFieldProtocol.ChannelContext)(implicit uiManager: DOMUIManager)
    extends DOMWidget[TextFieldProtocol.type, dom.html.Input](widgetId, uiManager) {
  import TextFieldProtocol._

  val artifact = {
    val el = tag[dom.html.Input]("input")
    el.addEventListener("input", onChange _)
    el.setAttribute("type", "text")
    el.value = context.initialValue
    DOMWidgetArtifact(el)
  }

  override protected def baseStyleClasses = List(DOMTextField.style.base)

  private def onChange(e: dom.Event): Unit = {
    channel.send(ValueChanged(artifact.el.value))
  }

  override def process = {
    case SetValue(text) =>
      modifyDOM { node =>
        // save cursor/selection
        val start = node.selectionStart
        val end   = node.selectionEnd
        node.value = text
        // restore
        node.setSelectionRange(start, end)
      }
    case msg =>
      super.process(msg)
  }

}

class DOMTextFieldBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TextFieldProtocol) {
  import TextFieldProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTextField(widgetId, context)(widgetManager)
}

object DOMTextField extends WidgetStyleProvider {
  class Style(uiManager: DOMUIManager) extends WidgetStyle()(uiManager) {
    import DOMWidget._

    val base = uiManager.registerCSSClass(palette => s"""
        |width: 100%;
        |height: ${show(styleConfig.inputHeight)};
        |line-height: ${show(styleConfig.inputHeight)};
        |border: solid ${show(styleConfig.inputBorderWidth)} ${show(styleConfig.inputBorderColor)};
        |border-radius: ${show(styleConfig.inputBorderRadius)};
        |padding-left: 0.5rem;
        |padding-right: 0.5rem;
        |color: ${show(palette(Palette.Base).color.foregroundColor)};
        |background-color: ${show(palette(Palette.Base).color.backgroundColor)};
        |font-family: ${styleConfig.fontFamily};
        |font-size: ${show(styleConfig.fontSize)};
        |font-weight: ${show(styleConfig.fontWeight)};
        """.stripMargin)
  }

  override def buildStyle(implicit uiManager: DOMUIManager) = new Style(uiManager)
}
