package suzaku.ui

import arteria.core.Protocol
import suzaku.ui.UIProtocol.UIChannel

trait WidgetBlueprint extends Blueprint {
  type P <: Protocol
  type This <: WidgetBlueprint
  type Proxy <: WidgetProxy[P, This]

  def createProxy(viewId: Int, uiChannel: UIChannel): Proxy

  def children: List[Blueprint] = Nil

  def sameAs(that: This): Boolean = equals(that)
}
