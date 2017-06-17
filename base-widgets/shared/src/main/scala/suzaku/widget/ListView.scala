package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object ListViewProtocol extends Protocol {

  sealed trait ListViewMessage extends Message

  case class SetDirection(direction: String) extends ListViewMessage

  private val mPickler = compositePickler[ListViewMessage]
    .addConcreteType[SetDirection]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext(direction: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object ListView extends WidgetBlueprintProvider {
  class WProxy private[ListView] (bd: WBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(ListViewProtocol, bd, viewId, uiChannel) {
    import ListViewProtocol._
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

  case class WBlueprint private[ListView] (direction: String)(content: List[Blueprint]) extends WidgetBlueprint {
    type P     = ListViewProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = content

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new WProxy(this)(viewId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(direction: String = "horz")(content: Blueprint*) = WBlueprint(direction)(content.toList)
}
