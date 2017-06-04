package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel

object LinearLayoutProtocol extends Protocol {

  sealed trait Direction

  object Direction {
    final case object Horizontal    extends Direction
    final case object HorizontalRev extends Direction
    final case object Vertical      extends Direction
    final case object VerticalRev   extends Direction
  }

  sealed trait Justify

  object Justify {
    final case object Start        extends Justify
    final case object End          extends Justify
    final case object Center       extends Justify
    final case object SpaceBetween extends Justify
    final case object SpaceAround  extends Justify
  }

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

object LinearLayout {
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

  def apply(direction: Direction = Direction.Horizontal, justify: Justify = Justify.Start)(content: Blueprint*) =
    WBlueprint(direction, justify)(content.toList)
}
