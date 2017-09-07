package suzaku.ui.layout

trait Layout {}

sealed trait Direction

object Direction {
  case object Horizontal    extends Direction
  case object HorizontalRev extends Direction
  case object Vertical      extends Direction
  case object VerticalRev   extends Direction
}

sealed trait Justify

object Justify {
  case object Start        extends Justify
  case object End          extends Justify
  case object Center       extends Justify
  case object SpaceBetween extends Justify
  case object SpaceAround  extends Justify
}
