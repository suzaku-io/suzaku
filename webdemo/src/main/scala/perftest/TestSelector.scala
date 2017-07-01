package perftest

import suzaku.ui._
import suzaku.ui.style.StyleClass
import suzaku.widget.Button

import scala.collection.immutable

case class TestInstance(name: String, component: ComponentBlueprint)

object TestSelector {
  final case class State private (isRunning: Boolean, currentTest: Int) {
    def selectTest(idx: Int): State = {
      State(isRunning = !(isRunning && idx == currentTest), currentTest = idx)
    }
  }

  final case class CBP private (tests: immutable.Seq[TestInstance]) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)(proxy)
  }

  final class ComponentImpl(initialBlueprint: CBP)(proxy: StateProxy)
      extends Component[CBP, State](initialBlueprint, proxy) {
    override def render(state: State): Blueprint = {
      import suzaku.ui.layout._
      import suzaku.ui.style._

      LinearLayout(Direction.Vertical)(
        LinearLayout()(
          // draw buttons for each test instance
          blueprint.tests.zipWithIndex.map {
            case (test, idx) =>
              Button(label = test.name, onClick = () => modState(s => s.selectTest(idx))) << (
                backgroundColor := (if (state.isRunning && state.currentTest == idx) Colors.lightblue else Colors.gray)
              )
          }
        ) << TestListStyle,
        if (state.isRunning) {
          LinearLayout()(blueprint.tests(state.currentTest).component) << TestPanelStyle
        } else EmptyBlueprint
      )
    }

    override def initialState: State = State(isRunning = false, 0)
  }

  def apply(tests: immutable.Seq[TestInstance]): CBP = CBP(tests)
}

object TestPanelStyle extends StyleClass {
  import suzaku.ui.style._

  def style = List(
    padding := 20.px
  )
}

object ButtonStyle extends StyleClass {
  import suzaku.ui.style._

  def style = List(
    padding := 5.px,
    margin := 10.px
  )
}

object TestListStyle extends StyleClass {
  import suzaku.ui.style._

  def style = List(
    widgetStyle := (Button -> List(ButtonStyle))
  )
}
