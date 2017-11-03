package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui._

object CheckboxProtocol extends WidgetProtocol {

  sealed trait CheckboxMessage extends Message

  case class SetValue(checked: Boolean) extends CheckboxMessage

  case class SetLabel(label: Option[String]) extends CheckboxMessage

  case class ValueChanged(value: Boolean) extends CheckboxMessage

  val mPickler = compositePickler[CheckboxMessage]
    .addConcreteType[SetValue]
    .addConcreteType[SetLabel]
    .addConcreteType[ValueChanged]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext(checked: Boolean, label: Option[String])

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
