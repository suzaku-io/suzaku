package suzaku.ui.resource

import suzaku.ui.BaseRegistry

case class ResourceRegistration(id: Int, resource: EmbeddedResource)

object EmbeddedResourceRegistry extends BaseRegistry[ResourceRegistration, EmbeddedResource] {
  def buildRegistryEntry(id: Int, data: EmbeddedResource, dataClass: Class[_ <: EmbeddedResource]): ResourceRegistration =
    ResourceRegistration(id, data)
}
