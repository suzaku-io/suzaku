package suzaku.ui.layout

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object LinearLayoutProtocol extends Protocol {

  sealed trait LayoutMessage extends Message

  final case class SetDirection(direction: Direction) extends LayoutMessage

  final case class SetJustify(justify: Justify) extends LayoutMessage

  private val mPickler = compositePickler[LayoutMessage]
    .addConcreteType[SetDirection]
    .addConcreteType[SetJustify]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  final case class ChannelContext(direction: Direction, justify: Justify)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object LinearLayout extends WidgetBlueprintProvider {
  import LinearLayoutProtocol._

  class WProxy private[LinearLayout] (bd: WBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(LinearLayoutProtocol, bd, viewId, uiChannel) {

    override def process = {
      case message =>
        super.process(message)
    }

    override def initView = ChannelContext(bd.direction, bd.justify)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.direction != blueprint.direction)
        send(SetDirection(newBlueprint.direction))
      if (newBlueprint.justify != blueprint.justify)
        send(SetJustify(newBlueprint.justify))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[LinearLayout] (direction: Direction, justify: Justify)(content: List[Blueprint])
      extends WidgetBlueprint {
    type P     = LinearLayoutProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = content

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new WProxy(this)(viewId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(direction: Direction = Direction.Horizontal, justify: Justify = Justify.Start)(content: Blueprint*) =
    WBlueprint(direction, justify)(content.toList)
}
