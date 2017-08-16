package suzaku.platform.web.widget

import org.scalajs.dom
import suzaku.platform.web.{DOMUIManager, DOMWidget, DOMWidgetArtifact}
import suzaku.ui.WidgetBuilder
import suzaku.widget.CheckboxProtocol

class DOMCheckbox(widgetId: Int, context: CheckboxProtocol.ChannelContext)(implicit uiManager: DOMUIManager)
    extends DOMWidget[CheckboxProtocol.type, dom.html.Label](widgetId, uiManager) {
  import CheckboxProtocol._

  val input = tag[dom.html.Input]("input")
  val labelText = textNode(context.label.getOrElse(""))

  val artifact = {
    val el = tag[dom.html.Label]("label")
    input.addEventListener("click", onChange _)
    input.setAttribute("type", "checkbox")
    input.checked = context.checked
    el.appendChild(input)
    el.appendChild(labelText)
    DOMWidgetArtifact(el)
  }

  override protected def baseStyleClasses = List(DOMCheckbox.style.base)

  def onChange(e: dom.Event): Unit = {
    channel.send(ValueChanged(input.checked))
  }

  override def process = {
    case SetValue(checked) =>
      modifyDOM(_ => input.checked = checked)
    case SetLabel(label) =>
      modifyDOM(_ => labelText.data = label.getOrElse(""))
    case msg =>
      super.process(msg)
  }
}

class DOMCheckboxBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(CheckboxProtocol) {
  import CheckboxProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMCheckbox(widgetId, context)(widgetManager)
}

object DOMCheckbox extends WidgetStyleProvider {
  class Style(uiManager: DOMUIManager) extends WidgetStyle()(uiManager) {
    val base = uiManager.registerCSSClass(palette =>
      s"""
         |display: inline-block;
         |margin-bottom: .25rem;
        """.stripMargin)
  }

  override def buildStyle(implicit uiManager: DOMUIManager) = new Style(uiManager)
}