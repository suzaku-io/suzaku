package suzaku.ui.style

abstract class StyleClassBase extends StyleClass {
  // register at initialization time
  val id = StyleClassRegistry.register(this, getClass)
}
