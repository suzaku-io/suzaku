package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui._

object TableProtocol extends WidgetProtocol {

  sealed trait TableMessage extends Message

  val mPickler = compositePickler[TableMessage]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext()

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

abstract class TableBaseProtocol extends WidgetProtocol {

  sealed trait TableMessage extends Message

  val mPickler = compositePickler[TableMessage]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext()

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object TableHeaderProtocol extends TableBaseProtocol
object TableFooterProtocol extends TableBaseProtocol
object TableBodyProtocol   extends TableBaseProtocol
object TableRowProtocol    extends TableBaseProtocol

abstract class TableBaseCellProtocol extends WidgetProtocol {

  sealed trait TableMessage extends Message

  case class SetSpans(colSpan: Int, rowSpan: Int) extends TableMessage

  val mPickler = compositePickler[TableMessage]
    .addConcreteType[SetSpans]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext(colSpan: Int, rowSpan: Int)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object TableCellProtocol       extends TableBaseCellProtocol
object TableHeaderCellProtocol extends TableBaseCellProtocol
