package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel

object LinearLayoutProtocol extends Protocol {

  sealed trait Direction

  final case object Horizontal extends Direction

  final case object Vertical extends Direction

  sealed trait LayoutMessage extends Message

  final case class SetDirection(direction: Direction) extends LayoutMessage

  private val mPickler = compositePickler[LayoutMessage]
    .addConcreteType[SetDirection]

  implicit val (messagePickler, witnessMsg) = defineProtocol(mPickler)

  final case class ChannelContext(direction: Direction)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object LinearLayout {
  import LinearLayoutProtocol._

  val Horizontal = LinearLayoutProtocol.Horizontal
  val Vertical   = LinearLayoutProtocol.Vertical

  class WProxy private[LinearLayout] (bd: WBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(LinearLayoutProtocol, bd, viewId, uiChannel) {

    override def process = {
      case message =>
        super.process(message)
    }

    override def initView = ChannelContext(bd.direction)

    override def update(newDesc: WBlueprint) = {
      if (newDesc.direction != blueprint.direction)
        send(SetDirection(newDesc.direction))
      super.update(newDesc)
    }
  }

  case class WBlueprint private[LinearLayout] (direction: Direction)(content: List[Blueprint]) extends WidgetBlueprint {
    type P     = LinearLayoutProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = content

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new WProxy(this)(viewId, uiChannel)
  }

  def apply(direction: Direction = Horizontal)(content: Blueprint*) = WBlueprint(direction)(content.toList)
}
