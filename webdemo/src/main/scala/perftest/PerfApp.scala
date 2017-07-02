package perftest

import suzaku.app.AppBase
import suzaku.platform.Transport

class PerfApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    val tests = List(
      TestInstance("Dummy", DummyTest()),
      TestInstance("Constant", ConstantUpdateTest()),
      TestInstance("O(n^2)", ConstantUpdateTest(slowDown = true, counter = 2000)),
      TestInstance("Table shuffle", TableTest(100, Shuffle)),
      TestInstance("Table grow", TableTest(100, Grow(1, 5000))),
      TestInstance("Table shrink", TableTest(1000, Shrink(1, 10)))
    )
    uiManager.render(TestSelector(tests))
  }
}
