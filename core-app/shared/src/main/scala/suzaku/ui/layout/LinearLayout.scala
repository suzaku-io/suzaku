package suzaku.ui.layout

import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object LinearLayout extends WidgetProtocolProvider {
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

  override def widgetProtocol = GridLayoutProtocol

  def apply(direction: Direction = Direction.Horizontal, justify: Justify = Justify.Start, align: Alignment = AlignAuto)(
      content: Blueprint*) =
    WBlueprint(direction, justify, align)(content.toList)
}
