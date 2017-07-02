package perftest

import java.util.UUID

import suzaku.ui._
import suzaku.ui.layout.{Direction, LinearLayout}
import suzaku.ui.style.StyleClass
import suzaku.widget.{Table, Text, TextField}

import scala.util.Random

sealed trait TableOp

case object Shuffle extends TableOp

case class Grow(amount: Int, maxSize: Int) extends TableOp

case class Shrink(amount: Int, minSize: Int) extends TableOp

case class DataRow(id: String, name: String, itemPrice: String, itemCount: String, totalPrice: String) {
}

object TableTest {
  final case class State private (data: Vector[DataRow])

  final case class CBP private (initialRows: Int, operation: TableOp) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)(proxy)
  }

  def genData(count: Int): Vector[DataRow] = {
    ((0 until count) map { _ =>
      val price = Random.nextDouble() * 100
      val count = Random.nextInt(100)
      DataRow(
        UUID.randomUUID().toString,
        String.valueOf(Array.tabulate[Char](8)(_ => Random.nextPrintableChar())),
        f"$price%.2f",
        count.toString,
        f"${price*count}%.2f"
      )
    }).toVector
  }

  final class ComponentImpl(initialBlueprint: CBP)(proxy: StateProxy)
      extends Component[CBP, State](initialBlueprint, proxy) {

    override def render(state: State): Blueprint = {
      import suzaku.ui.style._
      import suzaku.ui.Keywords.fixed
      import Table.Row

      LinearLayout(Direction.Vertical)(
        TextField(""),
        Table()
          .header("ID", "Name", "Item price", "Count", "Total")
          .body(state.data.map { data =>
            Row(data.id, data.name, data.itemPrice, data.itemCount.toString, data.totalPrice)
          .withKey(data.id)
          }) << (
          width := 600.px,
          tableLayout := fixed,
          widgetStyle := Table.HeaderCell -> List(FixedWidthCell)
        )
      )
    }

    override def initialState: State = State(Vector.empty)

    override def didMount(): Unit = {
      modState(_ => State(genData(blueprint.initialRows)))
    }

    override def didUpdate(nextBlueprint: CBP, nextState: State): Unit = {
      blueprint.operation match {
        case Shuffle =>
          //scala.scalajs.js.timers.setTimeout(1000)(
          modState(s => State(Random.shuffle(s.data)))
        //)
        case Grow(amount, maxSize) =>
          //scala.scalajs.js.timers.setTimeout(1000)(
            modState { s =>
              val data = (s.data ++ genData(amount)).take(maxSize)
              s.copy(data = data)
            }
          //)
        case Shrink(amount, minSize) =>
          //scala.scalajs.js.timers.setTimeout(1000)(
            modState { s =>
              val data = s.data.drop((s.data.size - minSize) min amount)
              s.copy(data = data)
            }
          //)
        case _ =>
      }
    }
  }

  def apply(initialRows: Int, operation: TableOp): CBP = CBP(initialRows, operation)
}

object FixedWidthCell extends StyleClass {
  import suzaku.ui.style._
  def style = List(
    width := 100.px
  )
}
