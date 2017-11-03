package suzaku.ui

trait ComponentBlueprint extends Blueprint {
  def create: Component[_ <: ComponentBlueprint, _]

  def sameAs(that: this.type): Boolean = equals(that)
}
