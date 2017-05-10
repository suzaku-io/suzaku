package suzaku.ui

import arteria.core.{Message, MessageChannel, MessageChannelHandler, Protocol}
import suzaku.{TestLogger, UnitSpec}
import boopickle.Default._
import org.scalamock.scalatest.MockFactory

object TestProtocol extends Protocol {
  override type ChannelContext = Unit

  sealed trait TestMessage extends Message

  private val tmPickler = compositePickler[TestMessage]

  implicit val (messagePickler, witnessMsg) = defineProtocol(tmPickler)

  override val contextPickler = implicitly[Pickler[Unit]]
}

class MockUIHandler extends MessageChannelHandler[UIProtocol.type]

class MockUIChannel extends MessageChannel(UIProtocol)(0, 0, null, new MockUIHandler, null) {}

class UIManagerSpec extends UnitSpec with MockFactory {

  class TestUIManager extends UIManager(TestLogger, _ => (), () => ()) {
    uiChannel = mock[MockUIChannel]

    (uiChannel
      .createChannel(_: TestProtocol.type)(_: MessageChannelHandler[TestProtocol.type],
        _: TestProtocol.type#ChannelContext,
        _: Any)(_: Pickler[Any]))
      .expects(*, *, *, *, *)

    def uic = uiChannel
  }

  "View manager child update" should {
    "update nothing" in {
      val vm = new TestUIManager
      val uiChannel = vm.uic

      uiChannel shouldNot be(null)
    }
  }
}
