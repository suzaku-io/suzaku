package suzaku.ui.layout

import arteria.core._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object LinearLayoutProtocol extends Protocol {
  import boopickle.Default._

  sealed trait LayoutMessage extends Message

  final case class SetDirection(direction: Direction) extends LayoutMessage

  final case class SetJustify(justify: Justify) extends LayoutMessage

  final case class SetAlignment(align: Alignment) extends LayoutMessage

  import Alignment._

  private val mPickler = compositePickler[LayoutMessage]
    .addConcreteType[SetDirection]
    .addConcreteType[SetJustify]
    .addConcreteType[SetAlignment]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  final case class ChannelContext(direction: Direction, justify: Justify, align: Alignment)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object LinearLayout extends WidgetBlueprintProvider {
  import LinearLayoutProtocol._

  class WProxy private[LinearLayout] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(LinearLayoutProtocol, bd, widgetId, uiChannel) {

    override def process = {
      case message =>
        super.process(message)
    }

    override def initWidget = ChannelContext(bd.direction, bd.justify, bd.align)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.direction != blueprint.direction)
        send(SetDirection(newBlueprint.direction))
      if (newBlueprint.justify != blueprint.justify)
        send(SetJustify(newBlueprint.justify))
      if (newBlueprint.align != blueprint.align)
        send(SetAlignment(newBlueprint.align))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[LinearLayout] (direction: Direction, justify: Justify, align: Alignment)(
      content: List[Blueprint])
      extends WidgetBlueprint {
    type P     = LinearLayoutProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = content

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new WProxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(direction: Direction = Direction.Horizontal, justify: Justify = Justify.Start, align: Alignment = AlignAuto)(
      content: Blueprint*) =
    WBlueprint(direction, justify, align)(content.toList)
}
