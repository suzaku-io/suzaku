package suzaku.ui

import suzaku.widget.Text.TextBlueprint

import scala.collection.immutable
import scala.language.implicitConversions

trait StateProxy {
  def modState[S](f: S => S): Unit
}

object StatelessProxy extends StateProxy {
  override def modState[S](f: S => S): Unit =
    throw new IllegalStateException("Cannot modify state of a stateless component")
}

abstract class Component[BP <: ComponentBlueprint, State](initialBlueprint: BP, proxy: StateProxy) {
  private[suzaku] var _blueprint: BP = initialBlueprint

  final def blueprint: BP = _blueprint

  protected final def modState(f: State => State): Unit = proxy.modState(f)

  def render(state: State): Blueprint

  def initialState: State

  def didMount(): Unit = {}

  def shouldUpdate(nextBlueprint: BP, state: State, nextState: State): Boolean = true

  def willReceiveBlueprint(nextBlueprint: BP): Unit = {}

  def didUpdate(nextBlueprint: BP, nextState: State): Unit = {}

  def didUnmount(): Unit = {}

  implicit def seqToBlueprint(seq: immutable.Seq[Blueprint]): BlueprintSeq = BlueprintSeq(seq.toList)

  implicit def stringToText(str: String): TextBlueprint = TextBlueprint(str)
}

abstract class StatelessComponent[BP <: ComponentBlueprint](bp: BP) extends Component[BP, AnyRef](bp, StatelessProxy) {
  def initialState: AnyRef = null

  def render: Blueprint

  final def render(state: AnyRef) = render
}
