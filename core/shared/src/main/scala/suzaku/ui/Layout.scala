package suzaku.ui

trait Layout {
}

sealed trait LayoutParameter

object LayoutParameter {
  case object NoLayout extends LayoutParameter
  case class Order(order: Int) extends LayoutParameter
}
