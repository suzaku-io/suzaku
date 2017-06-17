package suzaku.ui.layout

trait Layout {}

sealed trait Direction

object Direction {
  final case object Horizontal    extends Direction
  final case object HorizontalRev extends Direction
  final case object Vertical      extends Direction
  final case object VerticalRev   extends Direction
}

sealed trait Justify

object Justify {
  final case object Start        extends Justify
  final case object End          extends Justify
  final case object Center       extends Justify
  final case object SpaceBetween extends Justify
  final case object SpaceAround  extends Justify
}
