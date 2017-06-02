package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.style.StyleProperty
import suzaku.ui.style.StyleRegistry.StyleRegistration

object UIProtocol extends Protocol {

  type UIChannel = MessageChannel[UIProtocol.type]

  case class UIProtocolContext()

  override type ChannelContext = UIProtocolContext

  case class CreateWidget(widgetType: String, widgetId: Int)

  sealed trait ChildOp

  case class NoOp(count: Int = 1) extends ChildOp

  case class ReplaceOp(widgetId: Int) extends ChildOp

  case class InsertOp(widgetId: Int) extends ChildOp

  case class MoveOp(idx: Int) extends ChildOp

  case class RemoveOp(count: Int = 1) extends ChildOp

  implicit val opPickler = compositePickler[ChildOp]
    .addConcreteType[NoOp]
    .addConcreteType[ReplaceOp]
    .addConcreteType[InsertOp]
    .addConcreteType[MoveOp]
    .addConcreteType[RemoveOp]

  sealed trait UIMessage extends Message

  case class NextFrame(time: Long) extends UIMessage

  case object RequestFrame extends UIMessage

  case class MountRoot(widgetId: Int) extends UIMessage

  case class SetChildren(widgetId: Int, children: Seq[Int]) extends UIMessage

  case class UpdateChildren(widgetId: Int, ops: Seq[ChildOp]) extends UIMessage

  case class AddStyles(styles: List[StyleRegistration]) extends UIMessage

  implicit val stylePickler = StyleProperty.pickler

  private val uiPickler = compositePickler[UIMessage]
    .addConcreteType[NextFrame]
    .addConcreteType[RequestFrame.type]
    .addConcreteType[MountRoot]
    .addConcreteType[SetChildren]
    .addConcreteType[UpdateChildren]
    .addConcreteType[AddStyles]

  implicit val (messagePickler, uiMsgWitness) = defineProtocol(uiPickler)

  override val contextPickler = implicitly[Pickler[UIProtocolContext]]
}
