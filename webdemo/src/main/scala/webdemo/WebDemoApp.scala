package webdemo

import boopickle.Default.{Pickler, compositePickler}
import suzaku.app.AppBase
import suzaku.platform.Transport
import suzaku.ui._
import suzaku.ui.layout.LayoutId
import suzaku.ui.style.StyleClass
import suzaku.widget.{Button, Checkbox, TextInput}

object TestComp {
  import suzaku.ui.layout._
  case class State(count: Int,
                   time: Long,
                   text: String,
                   checked: Boolean,
                   direction: Direction = Direction.Horizontal,
                   justify: Justify = Justify.Start,
                   align: Alignment = AlignStretch)

  case class CBP private (label: String) extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)(proxy)
  }

  class ComponentImpl(initialBlueprint: CBP)(proxy: StateProxy) extends Component[CBP, State](initialBlueprint, proxy) {
    def render(state: State) = {
      import suzaku.ui.style._
      import suzaku.ui.layout._
      import suzaku.ui.KeywordTypes._

      LinearLayout(Direction.Vertical)(
        LinearLayout(state.direction, state.justify, state.align)(
          Checkbox(state.checked, value => modState(s => s.copy(checked = value))) << (
            width := 10.em
          ),
          TextInput(state.text, value => modState(s => s.copy(text = value))),
          Button(s"Add button ${state.count}", () => add()).withKey(0).withLayout(order := 2) << GreenButton,
          Button(s"Remove button ${state.count}", () => dec())
            .withKey(1)
            .withLayout(alignSelf := start)
            .withStyle(
              backgroundColor := rgb(128, 0, state.time.toInt * 16 & 0xFF),
              color := 0xFF80FF,
              RedButton,
              Large
            ),
          if (state.count == 0)
            EmptyBlueprint
          else
            for (i <- 0 until state.count) yield List(Button(s"A $i"), Button(s"B $i")): Blueprint,
          Button(
            s"Direction ${state.direction}",
            () => modState(state => state.copy(direction = flipDirection(state.direction)))
          ),
          Button(
            s"Justify ${state.justify}",
            () => modState(state => state.copy(justify = flipJustify(state.justify)))
          ),
          Button(
            s"Align ${state.align}",
            () => modState(state => state.copy(align = flipAlignment(state.align)))
          ),
          s"Just some <script>${"text" * state.count} </script>",
          Button(s"${blueprint.label} ${state.time}").withKey(3)
        ) << (
          if (state.checked) backgroundColor := rgb(0, 0, 0) else backgroundColor := rgb(255, 255, 255)
        ),
        GridLayout(
          400.px ~ 400.px ~ 200.px ~ 200.px,
          100.px ~ 300.px ~ 100.px,
          List(
            Layout1 :: Layout1 :: Layout1 :: Layout2 :: Nil,
            Layout3 :: Layout3 :: Layout3 :: Layout2 :: Nil,
            Layout3 :: Layout3 :: Layout3 :: Layout4 :: Nil
          )
        )(
          Button("1").withLayout(slot := Layout1) << (backgroundColor := Colors.blue),
          Button("2").withLayout(slot := Layout2) << (backgroundColor := Colors.green),
          Button("3").withLayout(slot := Layout3) << (backgroundColor := Colors.red)
        ).withLayout(weight := 1)
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

    def flipAlignment(align: Alignment): Alignment = {
      align match {
        case AlignAuto     => AlignStart
        case AlignStart    => AlignEnd
        case AlignEnd      => AlignCenter
        case AlignCenter   => AlignBaseline
        case AlignBaseline => AlignStretch
        case AlignStretch  => AlignStart
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

object Layout1 extends LayoutId
object Layout2 extends LayoutId
object Layout3 extends LayoutId
object Layout4 extends LayoutId

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
    height := 100.px
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
