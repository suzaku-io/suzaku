package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._
import suzaku.ui.resource.ImageResource

object ButtonProtocol extends Protocol {

  sealed trait ButtonMessage extends Message

  case class SetLabel(label: String) extends ButtonMessage

  case class SetIcon(icon: Option[ImageResource]) extends ButtonMessage

  case object Click extends ButtonMessage

  import ImageResource.pickler

  val mPickler = compositePickler[ButtonMessage]
    .addConcreteType[SetLabel]
    .addConcreteType[SetIcon]
    .addConcreteType[Click.type]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(label: String, icon: Option[ImageResource])

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object Button extends WidgetBlueprintProvider {
  class WProxy private[Button] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(ButtonProtocol, bd, widgetId, uiChannel) {
    import ButtonProtocol._

    override def process = {
      case Click =>
        blueprint.onClick.foreach(f => f())
      case message =>
        super.process(message)
    }

    override def initWidget = ChannelContext(bd.label, bd.icon)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.label != blueprint.label)
        send(SetLabel(newBlueprint.label))
      if (newBlueprint.icon != blueprint.icon)
        send(SetIcon(newBlueprint.icon))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Button] (label: String, icon: Option[ImageResource] = None, onClick: Option[() => Unit] = None) extends WidgetBlueprint {
    type P     = ButtonProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new WProxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(label: String) = WBlueprint(label, None, None)

  def apply(label: String, onClick: () => Unit) = WBlueprint(label, None, Some(onClick))

  def apply(label: String, icon: ImageResource) = WBlueprint(label, Some(icon), None)

  def apply(label: String, icon: ImageResource, onClick: () => Unit) = WBlueprint(label, Some(icon), Some(onClick))

  def apply(icon: ImageResource) = WBlueprint("", Some(icon), None)

  def apply(icon: ImageResource, onClick: () => Unit) = WBlueprint("", Some(icon), Some(onClick))
}
