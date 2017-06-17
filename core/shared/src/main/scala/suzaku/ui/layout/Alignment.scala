package suzaku.ui.layout

import suzaku.ui.Keywords
import scala.language.implicitConversions

sealed trait Alignment

case object AlignAuto     extends Alignment
case object AlignStart    extends Alignment
case object AlignEnd      extends Alignment
case object AlignCenter   extends Alignment
case object AlignBaseline extends Alignment
case object AlignStretch  extends Alignment

trait AlignmentImplicits {
  implicit def auto2align(a: Keywords.auto.type): Alignment         = AlignAuto
  implicit def start2align(a: Keywords.start.type): Alignment       = AlignStart
  implicit def end2align(a: Keywords.end.type): Alignment           = AlignEnd
  implicit def center2align(a: Keywords.center.type): Alignment     = AlignCenter
  implicit def baseline2align(a: Keywords.baseline.type): Alignment = AlignBaseline
  implicit def stretch2align(a: Keywords.stretch.type): Alignment   = AlignStretch
}

object Alignment {
  import boopickle.Default._

  implicit val alignmentPickler = compositePickler[Alignment]
    .addConcreteType[AlignAuto.type]
    .addConcreteType[AlignStart.type]
    .addConcreteType[AlignEnd.type]
    .addConcreteType[AlignCenter.type]
    .addConcreteType[AlignBaseline.type]
    .addConcreteType[AlignStretch.type]

}
