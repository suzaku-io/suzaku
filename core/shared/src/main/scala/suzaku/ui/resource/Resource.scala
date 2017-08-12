package suzaku.ui.resource

trait Resource {
  def isEmbedded: Boolean

  final def isExternal: Boolean = !isEmbedded
}

trait EmbeddedResource extends Resource {
  override def isEmbedded: Boolean = true

  val resourceId = EmbeddedResourceRegistry.register(this, getClass)
}

trait ExternalResource extends Resource {
  override def isEmbedded: Boolean = false
}

object EmbeddedResource {
  import boopickle.Default._

  val pickler: Pickler[EmbeddedResource] = compositePickler[EmbeddedResource]
    .addConcreteType[ReferenceEmbeddedImageResource]
    .addConcreteType[SVGImageResource]
    .addConcreteType[Base64ImageResource]
}
