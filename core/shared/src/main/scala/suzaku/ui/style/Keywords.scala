package suzaku.ui.style

object Keywords {
  case object auto
  case object none
  case object hidden
  case object solid
  case object dotted
  case object dashed
  case object inset
  case object outset
  case object thin
  case object medium
  case object thick
}

trait KeywordTypes {
  val auto   = Keywords.auto
  val none   = Keywords.none
  val hidden = Keywords.hidden
  val solid  = Keywords.solid
  val dotted = Keywords.dotted
  val dashed = Keywords.dashed
  val inset  = Keywords.inset
  val outset = Keywords.outset
  val thin   = Keywords.thin
  val medium = Keywords.medium
  val thick  = Keywords.thick
}
