package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.layout.LayoutIdRegistration
import suzaku.ui.resource.{EmbeddedImageResource, EmbeddedResource, ResourceRegistration}
import suzaku.ui.style.{Color, Palette, StyleClassRegistration, StyleProperty}

object UIProtocol extends Protocol {

  type UIChannel = MessageChannel[UIProtocol.type]

  case class UIProtocolContext()

  override type ChannelContext = UIProtocolContext

  case class CreateWidget(widgetClass: Int, widgetId: Int)

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

  case object FrameComplete extends UIMessage

  case class MountRoot(widgetId: Int) extends UIMessage

  case class SetChildren(widgetId: Int, children: List[Int]) extends UIMessage

  case class UpdateChildren(widgetId: Int, ops: List[ChildOp]) extends UIMessage

  case class AddStyles(styles: List[StyleClassRegistration]) extends UIMessage

  case class AddLayoutIds(ids: List[LayoutIdRegistration]) extends UIMessage

  case class AddResources(resources: List[ResourceRegistration]) extends UIMessage

  case class ActivateTheme(themeId: Int, theme: Map[Int, List[Int]]) extends UIMessage

  case class DeactivateTheme(themeId: Int) extends UIMessage

  case class RegisterWidgetClass(className: String, classId: Int) extends UIMessage

  case class SetPalette(palette: Palette) extends UIMessage

  implicit private val stylePickler = StyleProperty.stylePickler
  implicit private val palettePickler = Color.palettePickler
  implicit private val embeddedResourcePickler = EmbeddedResource.pickler

  
  private val uiPickler = compositePickler[UIMessage]
    .addConcreteType[NextFrame]
    .addConcreteType[RequestFrame.type]
    .addConcreteType[FrameComplete.type]
    .addConcreteType[MountRoot]
    .addConcreteType[SetChildren]
    .addConcreteType[UpdateChildren]
    .addConcreteType[AddStyles]
    .addConcreteType[AddLayoutIds]
    .addConcreteType[AddResources]
    .addConcreteType[ActivateTheme]
    .addConcreteType[DeactivateTheme]
    .addConcreteType[RegisterWidgetClass]
    .addConcreteType[SetPalette]

  implicit val (messagePickler, uiMsgWitness) = defineProtocol(uiPickler)

  override val contextPickler = implicitly[Pickler[UIProtocolContext]]
}
