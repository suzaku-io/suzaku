package suzaku.ui

import arteria.core._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.{StyleBaseProperty, StyleClass, StyleClasses, StylePropOrClass, StyleProperty, StyleSeq}

import scala.collection.mutable

trait WidgetBlueprint extends Blueprint {
  type P <: Protocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  private[suzaku] val _style  = mutable.Map.empty[Class[_], StyleProperty]
  private[suzaku] var _layout = List.empty[LayoutProperty]

  def createProxy(widgetId: Int, uiChannel: UIChannel): Proxy

  def children: List[Blueprint] = Nil

  def sameAs(that: This): Boolean = equals(that) && _style == that._style && _layout == that._layout

  final def <<(styleProperty: StylePropOrClass*): this.type = {
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

  @inline final def withLayout(layoutProperty: LayoutProperty*): this.type = {
    _layout = _layout ::: layoutProperty.toList
    this
  }
}

object WidgetProtocol extends Protocol {
  import boopickle.Default._

  implicit val stylePropertyPickler  = StyleProperty.stylePickler
  implicit val layoutPropertyPickler = LayoutProperty.layoutPickler

  sealed trait WidgetMessage extends Message

  case class UpdateStyle(params: List[(StyleProperty, Boolean)]) extends WidgetMessage

  case class UpdateLayout(params: List[LayoutProperty]) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateStyle]
    .addConcreteType[UpdateLayout]

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}

trait WidgetBlueprintProvider {
  def blueprintClass: Class[_ <: WidgetBlueprint]
}
