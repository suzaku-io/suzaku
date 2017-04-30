package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{Blueprint, WidgetBlueprint, WidgetProxy}

object ListViewProtocol extends Protocol {

  sealed trait ListViewMessage extends Message

  case class SetDirection(direction: String) extends ListViewMessage

  private val lvPickler = compositePickler[ListViewMessage]
    .addConcreteType[SetDirection]

  implicit val (messagePickler, witnessMsg) = defineProtocol(lvPickler)

  case class ChannelContext(direction: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object ListView {
  class ListViewProxy private[ListView] (bd: ListViewBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(ListViewProtocol, bd, viewId, uiChannel) {
    import ListViewProtocol._
    override def process = {
      case message =>
        super.process(message)
    }

    override def initView = ChannelContext(bd.direction)

    override def update(newDesc: ListViewBlueprint) = {
      if (newDesc.direction != blueprint.direction)
        send(SetDirection(newDesc.direction))
      super.update(newDesc)
    }
  }

  case class ListViewBlueprint private[ListView] (direction: String)(content: List[Blueprint]) extends WidgetBlueprint {
    type P     = ListViewProtocol.type
    type Proxy = ListViewProxy
    type This  = ListViewBlueprint

    override val children = content

    override def createProxy(viewId: Int, uiChannel: UIChannel) = new ListViewProxy(this)(viewId, uiChannel)

    def apply(children: Blueprint*): ListViewBlueprint = {
      ListViewBlueprint(direction)(children.toList)
    }
  }

  def apply(direction: String = "horz")(content: Blueprint*) = ListViewBlueprint(direction)(content.toList)
}
