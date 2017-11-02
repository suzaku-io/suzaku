package suzaku.ui

import arteria.core.{Message, Protocol}
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.StyleProperty

object WidgetExtProtocol extends Protocol {
  import boopickle.Default._

  implicit val stylePropertyPickler  = StyleProperty.stylePickler
  implicit val layoutPropertyPickler = LayoutProperty.layoutPickler

  sealed trait WidgetMessage extends Message

  case class UpdateStyle(params: List[(StyleProperty, Boolean)]) extends WidgetMessage

  case class UpdateLayout(params: List[LayoutProperty]) extends WidgetMessage

  sealed trait EventMessage

  case class OnClick(x: Int, y: Int, button: Int) extends WidgetMessage with EventMessage

  case class OnLongClick(x: Int, y: Int, button: Int) extends WidgetMessage with EventMessage

  case class OnFocusChange(focused: Boolean) extends WidgetMessage with EventMessage

  case class ListenTo(eventType: Int, active: Boolean = true) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateStyle]
    .addConcreteType[UpdateLayout]
    .addConcreteType[OnClick]
    .addConcreteType[OnLongClick]
    .addConcreteType[OnFocusChange]
    .addConcreteType[ListenTo]

  object EventType {
    final val OnClickEvent       = 1
    final val OnLongClickEvent   = 2
    final val OnFocusChangeEvent = 3
  }

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}
