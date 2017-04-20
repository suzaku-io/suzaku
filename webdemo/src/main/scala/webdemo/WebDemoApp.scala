package webdemo

import boopickle.Default.{Pickler, compositePickler}
import suzaku.app.AppBase
import suzaku.platform.Transport
import suzaku.ui._
import suzaku.widget.{Button, ListView}

object TestComp {
  case class State(count: Int, time: Long)

  case class CBP private (label: String) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)(proxy)
  }

  class ComponentImpl(initialBlueprint: CBP)(proxy: StateProxy) extends Component[CBP, State](initialBlueprint, proxy) {
    def render(state: State) = ListView()(
      Button(s"Add button ${state.count}", () => add()).withKey(0),
      Button(s"Remove button ${state.count}", () => dec()).withKey(1),
      if (state.count == 0)
        EmptyBlueprint
      else
        for (i <- 0 until state.count) yield Seq(Button(s"A $i"), Button(s"B $i")): Blueprint,
      Button("Click me too", () => add()).withKey(2),
      s"Just some <script>${"text"*state.count} </script>",
      Button(s"${blueprint.label} ${state.time}").withKey(3)
    )

    override def willReceiveBlueprint(nextBlueprint: CBP): Unit = {
      println(s"Will receive $nextBlueprint")
    }

    def initialState = State(0, 0)

    def add(): Unit = {
      modState(state => state.copy(count = state.count + 1))
    }

    def dec(): Unit = {
      modState(state => state.copy(count = math.max(0, state.count - 1)))
    }

    override def didMount(): Unit = {
      scala.scalajs.js.timers.setInterval(1000) {
        modState(state => state.copy(time = state.time + 1))
      }
    }
  }

  def apply(label: String = ""): CBP = CBP(label)
}

object StatelessTestComp {
  case class CBP private (label: String) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)
  }

  class ComponentImpl(initialBlueprint: CBP) extends StatelessComponent[CBP](initialBlueprint) {
    def render = ???
  }

  def apply(label: String = ""): CBP = CBP(label)
}

class WebDemoApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    val comp = TestComp("Testing")
    viewManager.render(comp)
  }

  def clicked(): Unit = {
    println("Clicked!")
  }
}
