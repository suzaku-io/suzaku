package suzaku.ui

import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.{StyleBaseProperty, StyleClass, StyleClasses, StylePropOrClass, StyleProperty, StyleSeq}

import scala.collection.immutable

trait WidgetBlueprint extends Blueprint {
  type P <: WidgetProtocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  private[suzaku] var _style                = immutable.Map.empty[Class[_], StyleProperty]
  private[suzaku] var _layout               = List.empty[LayoutProperty]
  private[suzaku] var _activeEvents         = Set.empty[Int]
  private[suzaku] var _onClickHandler       = Option.empty[(Int, Int, Int) => Unit]
  private[suzaku] var _onLongClickHandler   = Option.empty[(Int, Int, Int) => Unit]
  private[suzaku] var _onFocusChangeHandler = Option.empty[Boolean => Unit]

  def createProxy(widgetId: Int, uiChannel: UIChannel): Proxy

  def children: Seq[Blueprint] = Seq.empty

  def sameAs(that: This): Boolean =
    equals(that) && _style == that._style && _layout == that._layout && _activeEvents == that._activeEvents

  @noinline final def <<(styleProperty: StylePropOrClass*): this.type = {
    var styleClasses = List.empty[StyleClass]
    styleProperty.foreach {
      case StyleSeq(seq)        => _style ++= seq.map(p => (p.getClass, p))
      case s: StyleBaseProperty => _style += ((s.getClass, s))
      case c: StyleClass        => styleClasses ::= c
      case _                    =>
    }
    if (styleClasses.nonEmpty) {
      val prevClasses = _style.getOrElse(classOf[StyleClasses], StyleClasses(Nil)).asInstanceOf[StyleClasses]
      _style += (classOf[StyleClasses] -> StyleClasses(prevClasses.styles ::: styleClasses.reverse))
    }
    this
  }

  @inline final def withStyle(styleProperty: StylePropOrClass*): this.type = <<(styleProperty: _*)

  @noinline final def withLayout(layoutProperty: LayoutProperty*): this.type = {
    _layout = _layout ::: layoutProperty.toList
    this
  }

  @noinline final def withOnClick(handler: (Int, Int, Int) => Unit, disable: Boolean = false): this.type = {
    if (disable) {
      _activeEvents = _activeEvents - WidgetExtProtocol.EventType.OnClickEvent
      _onClickHandler = None
    } else {
      _activeEvents = _activeEvents + WidgetExtProtocol.EventType.OnClickEvent
      _onClickHandler = Some(handler)
    }
    this
  }

  @noinline final def withOnLongClick(handler: (Int, Int, Int) => Unit, disable: Boolean = false): this.type = {
    if (disable) {
      _activeEvents = _activeEvents - WidgetExtProtocol.EventType.OnLongClickEvent
      _onLongClickHandler = None
    } else {
      _activeEvents = _activeEvents + WidgetExtProtocol.EventType.OnLongClickEvent
      _onLongClickHandler = Some(handler)
    }
    this
  }

  @noinline final def withOnFocusChange(handler: Boolean => Unit, disable: Boolean = false): this.type = {
    if (disable) {
      _activeEvents = _activeEvents - WidgetExtProtocol.EventType.OnFocusChangeEvent
      _onFocusChangeHandler = None
    } else {
      _activeEvents = _activeEvents + WidgetExtProtocol.EventType.OnFocusChangeEvent
      _onFocusChangeHandler = Some(handler)
    }
    this
  }
}

trait WidgetProtocolProvider {
  def widgetProtocol: WidgetProtocol
}
