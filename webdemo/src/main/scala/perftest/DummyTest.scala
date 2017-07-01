package perftest

import suzaku.ui._
import suzaku.widget.Text

object DummyTest {
  final case class CBP private () extends ComponentBlueprint {
    override def create(proxy: StateProxy) = new ComponentImpl(this)
  }

  final class ComponentImpl(initialBlueprint: CBP) extends StatelessComponent(initialBlueprint) {
    override def render: Blueprint = {
      import suzaku.ui.style._

      Text("Dummy Test") << (
        fontSize := 2.rem
      )
    }
  }

  def apply(): CBP = CBP()
}
