package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.{WidgetExtProtocol, WidgetProtocol}

object TextFieldProtocol extends WidgetProtocol {

  sealed trait TextFieldMessage extends Message

  case class SetValue(value: String) extends TextFieldMessage

  case class ValueChanged(value: String) extends TextFieldMessage

  val mPickler = compositePickler[TextFieldMessage]
    .addConcreteType[SetValue]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext(initialValue: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
