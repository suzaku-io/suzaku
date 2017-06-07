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
  case object xxsmall
  case object xsmall
  case object small
  case object smaller
  case object large
  case object larger
  case object xlarge
  case object xxlarge

}

trait KeywordTypes {
  val auto    = Keywords.auto
  val none    = Keywords.none
  val hidden  = Keywords.hidden
  val solid   = Keywords.solid
  val dotted  = Keywords.dotted
  val dashed  = Keywords.dashed
  val inset   = Keywords.inset
  val outset  = Keywords.outset
  val thin    = Keywords.thin
  val medium  = Keywords.medium
  val thick   = Keywords.thick
  val xxsmall = Keywords.xxsmall
  val xsmall  = Keywords.xsmall
  val small   = Keywords.small
  val smaller = Keywords.smaller
  val large   = Keywords.large
  val larger  = Keywords.larger
  val xlarge  = Keywords.xlarge
  val xxlarge = Keywords.xxlarge
}
