package webdemo

import boopickle.Default.{Pickler, compositePickler}
import suzaku.app.AppBase
import suzaku.platform.Transport
import suzaku.ui._
import suzaku.ui.layout.{Direction, Justify}
import suzaku.ui.style.StyleClass
import suzaku.widget.{Button, Checkbox, TextInput}

object TestComp {
  case class State(count: Int,
                   time: Long,
                   text: String,
                   checked: Boolean,
                   direction: Direction = Direction.Horizontal,
                   justify: Justify = Justify.Start)

  case class CBP private (label: String) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)(proxy)
  }

  class ComponentImpl(initialBlueprint: CBP)(proxy: StateProxy) extends Component[CBP, State](initialBlueprint, proxy) {
    def render(state: State) = {
      import suzaku.ui.style._
      import suzaku.ui.layout._
      import suzaku.ui.KeywordTypes._

      LinearLayout(state.direction, state.justify)(
        Checkbox(state.checked, value => modState(s => s.copy(checked = value))) << (
          width := 10.em
        ),
        TextInput(state.text, value => modState(s => s.copy(text = value))),
        Button(s"Add button ${state.count}", () => add()).withKey(0) << (
          Order(2),
        ) << GreenButton,
        Button(s"Remove button ${state.count}", () => dec()).withKey(1).withLayout(alignSelf := start) << (
          backgroundColor := rgb(128, 0, state.time.toInt * 16 & 0xFF),
          color := 0xFF80FF
        )  << RedButton,
        if (state.count == 0)
          EmptyBlueprint
        else
          for (i <- 0 until state.count) yield List(Button(s"A $i"), Button(s"B $i")): Blueprint,
        Button(
          s"Direction",
          () => modState(state => state.copy(direction = flipDirection(state.direction)))
        ).withKey(2),
        Button(
          s"Justify",
          () => modState(state => state.copy(justify = flipJustify(state.justify)))
        ).withKey(2),
        s"Just some <script>${"text" * state.count} </script>",
        Button(s"${blueprint.label} ${state.time}").withKey(3)
      ) << (
        if (state.checked) backgroundColor := rgb(0, 0, 0) else backgroundColor := rgb(255, 255, 255)
      )
    }

    def flipDirection(direction: Direction): Direction = {
      import Direction._
      direction match {
        case Horizontal    => HorizontalRev
        case HorizontalRev => Vertical
        case Vertical      => VerticalRev
        case VerticalRev   => Horizontal
      }
    }

    def flipJustify(justify: Justify): Justify = {
      import Justify._
      justify match {
        case Start        => End
        case End          => Center
        case Center       => SpaceBetween
        case SpaceBetween => SpaceAround
        case SpaceAround  => Start
      }
    }

    override def willReceiveBlueprint(nextBlueprint: CBP): Unit = {
      println(s"Will receive $nextBlueprint")
    }

    def initialState = State(0, 0, "init", checked = false)

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

import suzaku.ui.style._
import suzaku.ui.KeywordTypes._

object BaseStyle extends StyleClass {
  def style = List(
    height := auto,
    backgroundColor := 0x0060FF,
    hover := (
      outlineWidth := thick
    )
  )
}

object ButtonStyle extends StyleClass {
  def style = List(
    extendClass := BaseStyle,
    backgroundColor := 0x006000,
    padding := (10.px, 20.px),
    margin := 10.px,
    outline := (thin, dotted, 0xFF00FF),
    hover := (
      backgroundColor := 0xFF00FF
    )
  )
}

object GreenButton extends StyleClass {
  def style = List(
    color := 0x00FF00
  )
}

object Large extends StyleClass {
  def style = List(
    height := auto
  )
}

object Red extends StyleClass {
  def style = List(
    color := 0xFF0000
  )
}

object RedButton extends StyleClass {
  def style = List(
    inheritClasses := (Large, Red),
    fontFamily := ("Times New Roman", "Times", "serif"),
    fontSize := xxlarge,
    fontWeight := 600
  )
}

object MyTheme {
  val theme = Theme(
    Button -> ButtonStyle
  )
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
    uiManager.activateTheme(MyTheme.theme)
    uiManager.render(comp)
  }
}
