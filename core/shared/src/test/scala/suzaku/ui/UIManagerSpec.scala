package suzaku.ui

import arteria.core.{Message, MessageChannel, MessageChannelHandler, Protocol}
import suzaku.{TestLogger, UnitSpec}
import boopickle.Default._
import org.scalamock.scalatest.MockFactory
import suzaku.ui.AnotherView.AnotherBlueprint
import suzaku.ui.TestView.{TestBlueprint, TestProxy}
import suzaku.ui.UIManager._
import suzaku.ui.UIProtocol._

object TestProtocol extends Protocol {
  override type ChannelContext = Unit

  sealed trait TestMessage extends Message

  case class Msg1(i: Int) extends TestMessage

  private val tmPickler = compositePickler[TestMessage]
    .addConcreteType[Msg1]

  implicit val (messagePickler, witnessMsg) = defineProtocol(tmPickler)

  override val contextPickler = implicitly[Pickler[Unit]]
}

trait BaseProxy {
  var updates: Int
}

object TestView {

  class TestProxy(bp: TestBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TestProtocol, bp, viewId, uiChannel)
      with BaseProxy {
    var updates                     = 0
    override protected def initView = {}

    override def update(newDesc: TestBlueprint): Unit = {
      updates += 1
    }
  }

  case class TestBlueprint(i: Int) extends WidgetBlueprint {
    override type P     = TestProtocol.type
    override type This  = TestBlueprint
    override type Proxy = TestProxy

    override def createProxy(viewId: Int, uiChannel: UIChannel): Proxy = new TestProxy(this)(viewId, uiChannel)
  }
}

object AnotherView {
  class AnotherProxy(bp: AnotherBlueprint)(viewId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TestProtocol, bp, viewId, uiChannel)
      with BaseProxy {
    var updates                     = 0
    override protected def initView = {}

    override def update(newDesc: AnotherBlueprint): Unit = {
      updates += 1
    }
  }

  case class AnotherBlueprint(s: String) extends WidgetBlueprint {
    override type P     = TestProtocol.type
    override type This  = AnotherBlueprint
    override type Proxy = AnotherProxy

    override def createProxy(viewId: Int, uiChannel: UIChannel): Proxy = new AnotherProxy(this)(viewId, uiChannel)
  }
}

class UIManagerSpec extends UnitSpec with MockFactory {
  class TestUIManager extends UIManager(TestLogger, _ => (), () => ()) {
    uiChannel = mock[MockUIChannel]

    (uiChannel
      .createChannel(_: TestProtocol.type)(_: MessageChannelHandler[TestProtocol.type],
                                           _: TestProtocol.type#ChannelContext,
                                           _: Any)(_: Pickler[Any]))
      .expects(*, *, *, *, *)
      .onCall { _ =>
        val m = mock[MockWidgetChannel]
        (m.close _).expects().noMoreThanOnce()
        m
      }
      .anyNumberOfTimes()

    def uic = uiChannel
  }

  class MockUIHandler extends MessageChannelHandler[UIProtocol.type]

  class MockUIChannel extends MessageChannel(UIProtocol)(0, 0, null, new MockUIHandler, null) {}

  class MockWidgetHandler extends MessageChannelHandler[TestProtocol.type]

  class MockWidgetChannel extends MessageChannel(TestProtocol)(0, 0, null, new MockWidgetHandler, ()) {}

  "View manager child update" should {
    "update nothing" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(0)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes.head.blueprint shouldBe TestBlueprint(0)
      nodes.head.asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      ops shouldBe Seq()
    }

    "update one child with same type" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic
      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes.head.blueprint shouldBe TestBlueprint(1)
      nodes.head.asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 1
      ops shouldBe Seq()
    }

    "replace with empty" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic
      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel)
      )
      val next = Seq(
        EmptyBlueprint
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 1
      ops shouldBe Seq(ReplaceOp(-1))
    }

    "add one child" in {
      val vm = new TestUIManager
      val current = Seq(
        )
      val next = Seq(
        TestBlueprint(1)
      )
      vm.viewId = 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes.head.blueprint shouldBe TestBlueprint(1)
      ops shouldBe Seq(InsertOp(nodes.head.getId))
    }

    "remove only child" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel)
      )
      val next = Seq(
        )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 0
      ops shouldBe Seq(RemoveOp())
    }

    "remove first child" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1), 2, None, uiChannel),
        new ShadowWidget(TestBlueprint(2), 3, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(1),
        TestBlueprint(2)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 2
      nodes(0).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 1
      nodes(1).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 1
      ops shouldBe Seq(NoOp(2), RemoveOp())
    }

    "remove last child" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1), 2, None, uiChannel),
        new ShadowWidget(TestBlueprint(2), 3, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(0),
        TestBlueprint(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 2
      ops shouldBe Seq(NoOp(2), RemoveOp())
    }

    "insert child to front" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1), 2, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(5),
        TestBlueprint(0),
        TestBlueprint(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 3
      ops shouldBe Seq(NoOp(2), InsertOp(nodes.last.getId))
    }

    "replace with another view" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0), 1, None, uiChannel)
      )
      val next = Seq(
        AnotherBlueprint("Kala")
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 1
      ops shouldBe Seq(ReplaceOp(nodes.last.getId))
    }

    "insert child to front using keys" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0).withKey(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1).withKey(1), 2, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(5).withKey(5),
        TestBlueprint(0).withKey(0),
        TestBlueprint(1).withKey(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 3
      nodes(0).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(1).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(2).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      ops shouldBe Seq(InsertOp(nodes.head.getId))
    }

    "insert child to middle using keys" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0).withKey(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1).withKey(1), 2, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(0).withKey(0),
        TestBlueprint(5).withKey(5),
        TestBlueprint(1).withKey(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 3
      nodes(0).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(1).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(2).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      ops shouldBe Seq(NoOp(), InsertOp(nodes(1).getId))
    }

    "reorder using keys" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0).withKey(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1).withKey(1), 2, None, uiChannel),
        new ShadowWidget(TestBlueprint(2).withKey(2), 3, None, uiChannel),
        new ShadowWidget(TestBlueprint(3).withKey(3), 4, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(2).withKey(2),
        TestBlueprint(0).withKey(0),
        TestBlueprint(3).withKey(3),
        TestBlueprint(1).withKey(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 4
      nodes(0).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(1).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(2).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(3).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      ops shouldBe Seq(MoveOp(2), NoOp(), MoveOp(3))
    }

    "manage invalid duplicate keys" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic

      val current = Seq(
        new ShadowWidget(TestBlueprint(0).withKey(0), 1, None, uiChannel),
        new ShadowWidget(TestBlueprint(1).withKey(1), 2, None, uiChannel),
        new ShadowWidget(TestBlueprint(2).withKey(1), 3, None, uiChannel),
        new ShadowWidget(TestBlueprint(3).withKey(0), 4, None, uiChannel)
      )
      val next = Seq(
        TestBlueprint(2).withKey(0),
        TestBlueprint(0).withKey(0),
        TestBlueprint(3).withKey(3),
        TestBlueprint(1).withKey(1)
      )
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 4
      nodes(0).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 1
      nodes(1).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(2).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      nodes(3).asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates shouldBe 0
      ops shouldBe Seq(NoOp(), InsertOp(nodes(1).getId), InsertOp(nodes(2).getId), NoOp(), RemoveOp(2))
    }

    "reorder reversed" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic
      val current   = (0 until 100).map(i => new ShadowWidget(TestBlueprint(i).withKey(i), i + 1, None, uiChannel))
      val next      = 99.to(0, -1).map(i => TestBlueprint(i).withKey(i))
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 100
      assert(nodes.forall(_.asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates == 0))
      ops shouldBe (0 until 99).map(_ => MoveOp(99))
    }

    "reorder reversed with different view" in {
      val vm        = new TestUIManager
      val uiChannel = vm.uic
      val current   = (0 until 100).map(i => new ShadowWidget(TestBlueprint(i).withKey(i), i + 1, None, uiChannel))
      val next      = 99.to(0, -1).map(i => AnotherBlueprint(s"$i").withKey(i))
      vm.viewId = current.map(_.widgetId).max + 1

      val (nodes, ops) = vm.updateChildren(null, current, next)
      nodes should have size 100
      assert(nodes.forall(_.asInstanceOf[ShadowWidget].proxy.asInstanceOf[BaseProxy].updates == 0))
      ops shouldBe (0 until 99).map(_ => MoveOp(99))
    }
  }
}
