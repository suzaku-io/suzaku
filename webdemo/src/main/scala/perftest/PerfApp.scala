package perftest

import suzaku.app.AppBase
import suzaku.platform.Transport

class PerfApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    val tests = List(
      TestInstance("Dummy", DummyTest()),
      TestInstance("Constant", ConstantUpdateTest()),
      TestInstance("O(n^2)", ConstantUpdateTest(slowDown = true, counter = 2000))
    )
    uiManager.render(TestSelector(tests))
  }
}
