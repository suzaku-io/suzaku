package suzaku.ui.layout

import suzaku.ui.BaseRegistry

object LayoutIdRegistry extends BaseRegistry[LayoutIdRegistration, LayoutId] {
  def buildRegistryEntry(id: Int, data: LayoutId, dataClass: Class[_ <: LayoutId]): LayoutIdRegistration = {
    LayoutIdRegistration(id, dataClass.getSimpleName)
  }
}
