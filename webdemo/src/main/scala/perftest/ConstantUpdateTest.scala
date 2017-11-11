package perftest

import suzaku.ui.layout.{Direction, LinearLayout}
import suzaku.ui.{Blueprint, Component, ComponentBlueprint}
import suzaku.widget.{Text, TextField}

object ConstantUpdateTest {
  final case class State private (counter: Int, value: Int)

  final case class CBP private (slowDown: Boolean, counter: Int) extends ComponentBlueprint {
    override def create = new ComponentImpl(this)
  }

  final class ComponentImpl(initialBlueprint: CBP) extends Component[CBP, State](initialBlueprint) {

    override def render(state: State): Blueprint = {
      import suzaku.ui.style._

      LinearLayout(Direction.Vertical)(
        TextField(""),
        LinearLayout()(
          Text(s"Count: ${state.counter}, value: ${state.value}") << (
            fontSize := 2.rem
          )
        )
      )
    }

    override def initialState: State = State(blueprint.counter, 0)

    override def didMount(): Unit = {
      modState(s => s.copy(counter = s.counter + 1))
    }

    override def didUpdate(nextBlueprint: CBP, nextState: State): Unit = {
      // waste some time
      if (blueprint.slowDown) {
        val sums = (0 to nextState.counter) map { i =>
          val l = List.tabulate[Int](nextState.counter)(identity)
          l.sum
        }
        modState(s => s.copy(value = sums.sum))
      }
      modState(s => s.copy(counter = s.counter + 1))
    }
  }

  def apply(slowDown: Boolean = false, counter: Int = 0): CBP = CBP(slowDown, counter)
}
