package suzaku.ui.layout

class LayoutIdBase extends LayoutId {
  override val id = LayoutIdRegistry.register(this, getClass)
}

object EmptySlot extends LayoutIdBase
