package suzaku.ui.layout

import suzaku.ui.BaseRegistry

case class LayoutIdRegistration(id: Int, className: String)

object LayoutIdRegistry extends BaseRegistry[LayoutIdRegistration, LayoutId] {
  def buildRegistryEntry(id: Int, data: LayoutId, dataClass: Class[_ <: LayoutId]): LayoutIdRegistration = {
    LayoutIdRegistration(id, dataClass.getSimpleName)
  }
}
