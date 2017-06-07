package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.style.{StyleBaseProperty, StyleClass, StyleClasses, StyleDef, StyleProperty, StyleSeq}

trait WidgetBlueprint extends Blueprint {
  type P <: Protocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  private[suzaku] var _style = Map.empty[Class[_], StyleProperty]

  def createProxy(viewId: Int, uiChannel: UIChannel): Proxy

  def children: List[Blueprint] = Nil

  def sameAs(that: This): Boolean = equals(that) && _style == that._style

  @inline final def <<<(styleProperty: StyleDef*): this.type = {
    val styles = styleProperty.flatMap {
      case StyleSeq(seq)        => seq
      case s: StyleBaseProperty => s :: Nil
      case _                    => Nil
    }
    _style ++= styles.map(p => (p.getClass, p))
    this
  }

  @inline final def <<(styleId: StyleClass*): this.type = {
    _style += (classOf[StyleClasses] -> StyleClasses(List(styleId: _*)))
    this
  }
}

object WidgetProtocol extends Protocol {
  implicit val stylePropertyPickler = StyleProperty.pickler

  sealed trait WidgetMessage extends Message

  case class UpdateStyle(params: List[(StyleProperty, Boolean)]) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateStyle]

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}

trait WidgetBlueprintProvider {
  def blueprintClass: Class[_ <: WidgetBlueprint]
}
