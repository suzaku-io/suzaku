package suzaku.ui.resource

import scala.reflect.ClassTag

trait ImageResource extends Resource {
  def mimeType: Option[String] = None
}

abstract class EmbeddedImageResource extends EmbeddedResource with ImageResource {
}

case class ReferenceEmbeddedImageResource(override val resourceId: Int) extends EmbeddedImageResource {
}

case class SVGImageResource(data: String, viewBox: (Int, Int, Int, Int)) extends EmbeddedImageResource {
  override def mimeType = Some("image/svg+xml")
}

case class Base64ImageResource(data: String, width: Int, height: Int, override val mimeType: Option[String] = None) extends EmbeddedImageResource

case class URIImageResource(uri: String, override val mimeType: Option[String] = None)
    extends ExternalResource
    with ImageResource

object EmbeddedImageResource {
  import boopickle.Default._

  val referencePickler: Pickler[EmbeddedImageResource] =
    transformPickler[EmbeddedImageResource, Int](id => ReferenceEmbeddedImageResource(id))(_.resourceId)
}

object ImageResource {
  import boopickle.Default._

  implicit val pickler: Pickler[ImageResource] = compositePickler[ImageResource]
    .addConcreteType(
      EmbeddedImageResource.referencePickler.asInstanceOf[Pickler[ReferenceEmbeddedImageResource]],
      ClassTag(classOf[ReferenceEmbeddedImageResource]))
    .addConcreteType(
      EmbeddedImageResource.referencePickler.asInstanceOf[Pickler[SVGImageResource]],
      ClassTag(classOf[SVGImageResource]))
    .addConcreteType(
      EmbeddedImageResource.referencePickler.asInstanceOf[Pickler[Base64ImageResource]],
      ClassTag(classOf[Base64ImageResource]))
    .addConcreteType[URIImageResource]

}
