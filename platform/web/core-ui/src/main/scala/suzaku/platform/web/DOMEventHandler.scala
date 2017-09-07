package suzaku.platform.web

import org.scalajs.dom

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSBracketAccess

sealed abstract class EventType(val id: Int, val name: String) {
  type Event <: dom.Event
}

case object BlurEvent      extends EventType(id = 0, name = "blur")       { type Event = dom.FocusEvent    }
case object FocusEvent     extends EventType(id = 1, name = "focus")      { type Event = dom.FocusEvent    }
case object ClickEvent     extends EventType(id = 2, name = "click")      { type Event = dom.MouseEvent    }
case object InputEvent     extends EventType(id = 3, name = "input")      { type Event = dom.Event         }
case object KeyDownEvent   extends EventType(id = 4, name = "keydown")    { type Event = dom.KeyboardEvent }
case object KeyUpEvent     extends EventType(id = 5, name = "keyup")      { type Event = dom.KeyboardEvent }
case object MouseDownEvent extends EventType(id = 6, name = "mousedown")  { type Event = dom.MouseEvent    }
case object MouseUpEvent   extends EventType(id = 7, name = "mouseup")    { type Event = dom.MouseEvent    }
case object MouseMoveEvent extends EventType(id = 8, name = "mousemove")  { type Event = dom.MouseEvent    }
case object MouseOutEvent  extends EventType(id = 9, name = "mouseout")   { type Event = dom.MouseEvent    }
case object MouseOverEvent extends EventType(id = 10, name = "mouseover") { type Event = dom.MouseEvent    }

@js.native
trait UnsafeJSDict[A] extends js.Any {
  @JSBracketAccess
  def apply(prop: String): js.UndefOr[A]
  @JSBracketAccess
  def update(prop: String, value: A): Unit
}

class InternalData extends js.Object {
  val listeners: UnsafeJSDict[DOMEvent[_ <: dom.Event] => Unit] =
    (new js.Object).asInstanceOf[UnsafeJSDict[DOMEvent[_ <: dom.Event] => Unit]]
  val captureListeners: UnsafeJSDict[DOMEvent[_ <: dom.Event] => Unit] =
    (new js.Object).asInstanceOf[UnsafeJSDict[DOMEvent[_ <: dom.Event] => Unit]]
  var widgetId = 0
}

class DOMEvent[E <: dom.Event](var nativeEvent: E) {
  var stop = false
  def stopPropagation(): Unit = {
    nativeEvent.stopPropagation()
    stop = true
  }
}

class DOMEventHandler(root: dom.EventTarget) {
  type EventCallback[E <: dom.Event] = DOMEvent[E] => Unit

  final val suzakuInternal = "__suzakuInternal"

  private val eventTypes = List[EventType](
    BlurEvent,
    FocusEvent,
    ClickEvent,
    InputEvent,
    KeyDownEvent,
    KeyUpEvent,
    MouseDownEvent,
    MouseUpEvent,
    MouseMoveEvent,
    MouseOutEvent,
    MouseOverEvent
  )

  private val listenerFunctions = eventTypes.map { et =>
    et.id -> (listener(et.id.toString): js.Function1[dom.Event, Unit])
  }.toMap

  private val registeredListeners =
    Array.fill[mutable.HashSet[Int]](eventTypes.map(_.id).max + 1)(mutable.HashSet.empty)
  private val registeredCaptureListeners =
    Array.fill[mutable.HashSet[Int]](eventTypes.map(_.id).max + 1)(mutable.HashSet.empty)

  private val handlerSeq        = Array.ofDim[EventCallback[dom.Event]](256)
  private val captureHandlerSeq = Array.ofDim[EventCallback[dom.Event]](256)

  def initialize(): Unit = {}

  def elementInternal(element: js.Dynamic): InternalData = {
    var internal = element.selectDynamic(suzakuInternal).asInstanceOf[InternalData]
    if (js.isUndefined(internal)) {
      internal = new InternalData
      element.updateDynamic(suzakuInternal)(internal)
    }
    internal
  }

  def addListener(
      eventType: EventType)(widgetId: Int, target: dom.Element, callback: EventCallback[eventType.Event]): Unit = {
    val listeners = registeredListeners(eventType.id)
    if (listeners.isEmpty)
      root.addEventListener(eventType.name, listenerFunctions(eventType.id))
    listeners.add(widgetId)
    val internal = elementInternal(target.asInstanceOf[js.Dynamic])
    internal.widgetId = widgetId
    internal.listeners(eventType.id.toString) = callback.asInstanceOf[DOMEvent[_ <: dom.Event] => Unit]
  }

  def removeListener(eventType: EventType, widgetId: Int, target: dom.Element): Unit = {
    val listeners = registeredListeners(eventType.id)
    listeners.remove(widgetId)
    val internal = elementInternal(target.asInstanceOf[js.Dynamic])
    js.special.delete(internal.listeners, eventType.id.toString)
    if (listeners.isEmpty)
      root.removeEventListener(eventType.name, listenerFunctions(eventType.id))
  }

  def addCaptureListener(
      eventType: EventType)(widgetId: Int, target: dom.Element, callback: EventCallback[eventType.Event]): Unit = {
    val listeners = registeredCaptureListeners(eventType.id)
    if (listeners.isEmpty)
      root.addEventListener(eventType.name, listenerFunctions(eventType.id))
    listeners.add(widgetId)
    val internal = elementInternal(target.asInstanceOf[js.Dynamic])
    internal.widgetId = widgetId
    internal.captureListeners(eventType.id.toString) = callback.asInstanceOf[DOMEvent[_ <: dom.Event] => Unit]
  }

  def removeCaptureListener(eventType: EventType, widgetId: Int, target: dom.Element): Unit = {
    val listeners = registeredCaptureListeners(eventType.id)
    listeners.remove(widgetId)
    val internal = elementInternal(target.asInstanceOf[js.Dynamic])
    js.special.delete(internal.captureListeners, eventType.id.toString)
    if (listeners.isEmpty)
      root.removeEventListener(eventType.name, listenerFunctions(eventType.id))
  }

  private def collectParents(eventType: String, target: js.Dynamic): (Int, Int) = {
    var idx    = 0
    var idxCap = 0
    var el     = target
    while (el != null) {
      val internal = el.__suzakuInternal.asInstanceOf[InternalData]
      if (!js.isUndefined(internal)) {
        internal.listeners(eventType).foreach { cb =>
          handlerSeq(idx) = cb
          idx += 1
        }
        internal.captureListeners(eventType).foreach { cb =>
          captureHandlerSeq(idxCap) = cb
          idxCap += 1
        }
      }
      el = el.parentNode
    }
    (idx, idxCap)
  }

  def listener(eventType: String) = (event: dom.Event) => {
    val target                              = event.target.asInstanceOf[js.Dynamic]
    val (handlerCount, captureHandlerCount) = collectParents(eventType, target)
    var i                                   = captureHandlerCount - 1
    val ev                                  = new DOMEvent(event)
    // do event capture phase
    while (i >= 0 && !ev.stop) {
      val handler = captureHandlerSeq(i)
      handler(ev)
      i -= 1
    }
    i = 0
    // do event bubbling phase
    while (i < handlerCount && !ev.stop) {
      val handler = handlerSeq(i)
      handler(ev)
      i += 1
    }
  }
}
