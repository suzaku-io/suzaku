package webdemo

import boopickle.Default.{Pickler, compositePickler}
import suzaku.app.AppBase
import suzaku.platform.Transport
import suzaku.ui._
import suzaku.ui.layout.LayoutIdBase
import suzaku.ui.resource.{Base64ImageResource, SVGImageResource}
import suzaku.ui.style.StyleClassBase
import suzaku.widget._

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
          Checkbox(state.checked, "Checked", value => modState(s => s.copy(checked = value))) << (
            width := 10.em
          ),
          TextField(state.text, value => modState(s => s.copy(text = value))),
          Button(s"Add button ${state.count}", () => add()).withKey(0).withLayout(order := 2) << GreenButton,
          Button(s"Remove button ${state.count}", EmbeddedTest.icon2, () => dec())
            .withKey(1)
            .withLayout(alignSelf := start)
            .withStyle(
              fromPalette := Palette.Secondary
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
          backgroundColor := (if (state.checked) rgb(128, 128, 136) else rgb(255, 255, 255))
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
          Table()
            .header("ID", "Name", "Email")
            .body(
              Table.Row(Table.Cell("123").rowSpan(2), "John Dow", Button("john@dow.com")),
              Table.Row(Table.Cell("Some Other").colSpan(2))
            )
            .footer("ID", "Name", "Email") << RedTable
        ).withLayout(weight := 1) << (
          gridRowGap := 10.px,
          gridColGap := 10.px
        )
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

object Layout1 extends LayoutIdBase
object Layout2 extends LayoutIdBase
object Layout3 extends LayoutIdBase
object Layout4 extends LayoutIdBase

object EmbeddedTest {
  lazy val icon = SVGImageResource(
    """<path d="M224 387.814V512L32 320l192-192v126.912C447.375 260.152 437.794 103.016 380.93 0 521.287 151.707 491.48 394.785 224 387.814z"/>""",
    (0, 0, 512, 512)
  )
  lazy val icon2 = Base64ImageResource(
    "R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7",
    16,
    16,
    Some("image/gif")
  )
}

object BaseStyle extends StyleClassBase {
  def styleDefs = List(
    hover := (
      outlineWidth := thick
    )
  )
}

object ButtonStyle extends StyleClassBase {
  def styleDefs = List(
    extendClass := BaseStyle
  )
}

object GreenButton extends StyleClassBase {
  def styleDefs = List(
    color := 0x00FF00
  )
}

object Large extends StyleClassBase {
  def styleDefs = List(
    height := 100.px
  )
}

object Red extends StyleClassBase {
  def styleDefs = List(
    color := 0xFF0000
  )
}

object GreenBackground extends StyleClassBase {
  def styleDefs = List(
    nthChild(2) := (
      backgroundColor := Colors.secondary
    )
  )
}

object BlueBackground extends StyleClassBase {
  def styleDefs = List(
    backgroundColor := Colors.primary
  )
}

object RedBackground extends StyleClassBase {
  def styleDefs = List(
    backgroundColor := 0xFF0000,
    widgetStyle := Table.Row -> List(GreenBackground)
  )
}

object RedButton extends StyleClassBase {
  def styleDefs = List(
    inheritClasses := (Large, Red),
    fontFamily := ("Times New Roman", "Times", "serif"),
    fontSize := xxlarge,
    fontWeight := 600
  )
}

object RedTable extends StyleClassBase {
  def styleDefs = List(
    widgetStyle := Table.Body -> List(RedBackground)
  )
}

object BlueTable extends StyleClassBase {
  def styleDefs = List(
    widgetStyle := Table.Row -> List(BlueBackground)
  )
}

object MyTheme {
  val theme = Theme(
    Button -> List(ButtonStyle),
    Table  -> List(BlueTable)
  )

  val palette = Palette(
    PaletteEntry(0xFEFEFE),
    PaletteEntry(Colors.blue),
    PaletteEntry(Colors.green),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red),
    PaletteEntry(Colors.red)
  )
}

class WebDemoApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    val comp = TestComp("Testing")
    uiManager.setPalette(MyTheme.palette)
    val themeId = uiManager.activateTheme(MyTheme.theme)
    uiManager.render(comp)
  }
}
